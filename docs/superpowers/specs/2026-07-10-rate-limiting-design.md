# Rate Limiting for Auth Endpoints — Design

**Ticket:** BOO-8 — Add rate limiting to auth endpoints

**Goal:** Stop unlimited, zero-cost automated abuse of the unauthenticated auth endpoints — credential brute-forcing on login, password-reset spam, bot mass account creation on register, and invite-list enumeration — while keeping the mechanism generic enough that adding rate limiting to a new endpoint later is a config/annotation change, not new plumbing.

## Scope

In scope: `POST /auth/login`, `POST /auth/password-reset`, `POST /auth/register`, `GET /auth/invitations`.

`GET /auth/invitations` (`AuthenticationController#checkInvite`) was added during design review: it's on the same permit-all `/api/*/auth/**` path as the other three, takes an email and returns whether it's invited — letting anyone enumerate the invite list for free by scripting through email addresses. Same profile (unauthenticated, zero-cost, IP-keyable) as the rest of this ticket's scope.

Out of scope (explicitly, not silently dropped):
- Rate limiting any other endpoint. Every other route in this app already requires a valid JWT (`SecurityConfig` permits only `/api/*/auth/**` and swagger/h2-console; everything else is `authenticated()`). Anonymous, zero-cost abuse — the threat this ticket addresses — is only possible against the three endpoints above. Authenticated-endpoint abuse (e.g. booking-creation scraping) is a different threat model (would key by user ID, not IP, and needs per-endpoint usage data to size limits sensibly) and belongs in its own ticket if it's ever observed.
- Email verification at registration (proving the registrant owns the email address) and login 2FA. Both are legitimate, separate hardening ideas raised during design discussion, but are distinct mechanisms (new account states, token/link flows, OTP delivery) that deserve their own design — not part of this ticket.

## Architecture

A custom `@RateLimit` annotation plus a Spring AOP `@Around` aspect (`RateLimitAspect`) wraps the four controller methods. Flow per request:

1. Spring MVC resolves and validates method arguments as normal (`@Valid` on the request body already ran).
2. The aspect resolves a **key** for the request — client IP, or a value pulled from the (already-validated) request body via SpEL.
3. It asks Bucket4j for the bucket at `{bucket-name}:{key}`, backed by a Caffeine cache (`ProxyManager`), creating the bucket on first use with limits from `RateLimitProperties`.
4. It attempts to consume one token. If available, the controller method proceeds. If not, it throws `RateLimitExceededException`.

Because this is AOP around the controller method (not a raw servlet `Filter`), the exception flows through Spring MVC's normal exception-resolution path and is handled by the existing `UniversalExceptionHandler`, like every other error in this app — no bespoke response-writing in a filter.

`JwtAuthenticationFilter` needs no changes: it already runs harmlessly ahead of these requests (no token present, route is permit-all), one layer outside where the aspect operates.

## Why Bucket4j (not Resilience4j, not hand-rolled)

Bucket4j's `ProxyManager` is built around "get-or-create a bucket for key X," backed by a cache with automatic eviction — a direct match for "one independent limiter per IP/email," of which there will be many, created dynamically. Resilience4j's `RateLimiter` is designed to guard a single shared resource (one named limiter, configured once); using it here would mean hand-rolling a `ConcurrentHashMap<String, RateLimiter>` and managing eviction ourselves — reintroducing the memory-leak risk a keyed limiter is supposed to avoid. A hand-rolled Caffeine counter was also considered and rejected: it would reimplement token-bucket refill logic Bucket4j already provides correctly, with a real edge case (fixed-window boundary bursts) that Bucket4j's greedy refill avoids.

**Storage backend:** Caffeine (in-memory), not Redis. Nothing in this repo's deployment (single `Dockerfile`, no LB/ingress, no Redis in `docker-compose`) indicates multi-instance deployment today. Bucket4j's `ProxyManager` abstraction means swapping Caffeine for Redis later (if the app ever scales to multiple instances) doesn't require touching the annotation/aspect code — only the `ProxyManager` bean wiring.

## Client IP resolution

`ClientIpResolver` (single bean, `request.getRemoteAddr()`) — deliberately **not** trusting `X-Forwarded-For` or enabling `server.forwarded-headers-strategy: framework`. Confirmed via repo inspection: there is no reverse proxy, load balancer, or CDN in front of this app today. Spring's forwarded-header support does not validate that a header came from a trusted hop — it trusts whatever is present. Enabling it without a trusted proxy in front would let a client set an arbitrary `X-Forwarded-For` value per request and defeat the rate limiter entirely, which is worse than doing nothing.

**Assumption / risk to revisit:** if this app is ever deployed behind a real reverse proxy or load balancer, `getRemoteAddr()` will return the proxy's IP for every request, collapsing all clients into one shared bucket. At that point, switch `ClientIpResolver` to trust forwarded headers — but only once it's confirmed the proxy strips/overwrites any client-supplied `X-Forwarded-For` before forwarding. `ClientIpResolver` is a single, isolated bean specifically so this is a one-place change when that day comes.

## Components

- `@RateLimit(bucket = "login", key = RateLimitKeyType.IP)` on `AuthenticationController#login`.
- `@RateLimit(bucket = "passwordReset", key = RateLimitKeyType.SPEL, keyExpression = "#request.email()")` on `resetPassword`.
- `@RateLimit(bucket = "register", key = RateLimitKeyType.IP)` on `register`.
- `@RateLimit(bucket = "checkInvite", key = RateLimitKeyType.IP)` on `checkInvite`.
- `RateLimitAspect` — the `@Around` advice described above.
- `ClientIpResolver` — resolves the key for IP-based buckets.
- `RateLimitProperties` (`@ConfigurationProperties(prefix = "rate-limit")`) — nested static `Login` / `PasswordReset` / `Register` / `CheckInvite` classes (`capacity`, `refillPeriod`, `refillStrategy`), following the existing `EmailProperties` / `ScheduleProperties` convention in this repo.
- `RateLimitExceededException` — new exception, mapped in `UniversalExceptionHandler` to `429`, using the existing `ErrorModel` shape, plus a `Retry-After` header computed from Bucket4j's `ConsumptionProbe.getNanosToWaitForRefill()`.

## Endpoints and default limits

All limits below are defaults, fully configurable via `rate-limit.*` properties — not hardcoded, per the ticket's acceptance criteria.

| Endpoint | Key | Capacity | Refill | Strategy |
|---|---|---|---|---|
| `POST /auth/login` | Client IP | 10 | 10 tokens / 1 minute | Greedy |
| `POST /auth/password-reset` | Email (request body) | 3 | 3 tokens / 15 minutes | Greedy |
| `POST /auth/register` | Client IP | 10 | 10 tokens / 1 hour | Greedy |
| `GET /auth/invitations` | Client IP | 20 | 20 tokens / 1 minute | Greedy |

Notes:
- **Invitations: 20/minute/IP.** This is a read-only lookup (no email sent, no account created), so the cost of a legitimate burst is low — the limit here exists purely to stop email-list enumeration/scraping at scale, not to protect a scarce resource. Generous enough not to interfere with a real registration flow's own invite check.
- **Login: 10/minute, not the ticket's literal 5/minute.** Discussed explicitly during design: the security difference between 5 and 10 attempts/minute against a real password's keyspace is negligible (both make online brute-forcing impractical), while 10 meaningfully reduces false-positive lockouts from typos/autofill/caps-lock. Keyed by IP (not account), which also avoids the OWASP-flagged risk of account-based lockout being usable as a DoS against a legitimate user.
- **Greedy refill is the default for all three buckets**, not intervally (fixed-window) refill. Greedy replenishes tokens continuously (e.g. login's 10/min ≈ 1 token every 6s) rather than resetting the whole bucket at a clock boundary, avoiding the classic fixed-window flaw where a client could burst, wait a fraction of a second past the boundary, and immediately burst again for up to 2x the intended limit.
- **Register's limit (10/hour/IP) is not specified by the ticket** — it was added during design discussion since `/auth/register` shares the same unauthenticated, zero-cost profile as login/password-reset. Starting default is generous enough to not block legitimate shared-IP signups (e.g. an office) while still blocking bot mass-registration; expected to be tuned from real usage/abuse data post-launch.

## Error handling

`RateLimitExceededException` → `@ExceptionHandler` in `UniversalExceptionHandler` → HTTP `429`, body via the existing `ErrorModel(timestamp, status, error, message, details)` record, plus a `Retry-After` header (seconds until next token, from `ConsumptionProbe`).

## Testing

- **Unit (`RateLimitAspectTest`):** request N+1 within the window is blocked; independent keys (different IPs/emails) get independent buckets; bucket refills correctly after time passes — using a controllable/fake clock, not real sleeps.
- **Controller (`AuthenticationControllerTest`, `@WebMvcTest`):** extend with cases like `givenLimitExceeded_whenLogin_thenReturns429`, following the existing given/when/then naming convention in this file.
- **Integration (`booking-system-it` module):** new IT hitting real HTTP endpoints repeatedly to confirm end-to-end 429 behavior including the `Retry-After` header — new addition to this module, since there's no existing auth IT to extend (current ITs are repository-layer only).

## Open assumptions to revisit later

- No reverse proxy/LB in front of the app today — `ClientIpResolver` will need updating if that changes (see Client IP resolution section).
- Register's rate limit numbers are a starting guess, not derived from real traffic/abuse data — expect to tune post-launch.
- Email verification at registration and login 2FA are known, separate gaps raised during design; intentionally out of scope here (see Scope section).
