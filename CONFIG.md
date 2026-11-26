# Configuration inventory

One table of every variable the two apps and their workflows read. Production is set up from this
table.

## Where each kind of setting lives

| Kind | Lives in | Rule |
| --- | --- | --- |
| Not secret, same everywhere | `application.yaml` | `${ENV_VAR:default}` placeholders are fine |
| Not secret, differs per environment, rarely changes | `application-local.yaml`, `application-dev.yaml`, `application-prod.yaml` | Log levels, `show-sql`, Actuator exposure, local SMTP host and port |
| Secrets, and anything that should change without a rebuild | Render environment variables | Never in git, not even in `application-prod.yaml` |
| What CI workflows need | GitHub secrets | Deploy hooks, the backup job |
| UI `NEXT_PUBLIC_*` | Render, **at build time** | Baked into the JavaScript and visible to anyone who opens the site. No secrets, and a change needs a redeploy |

## Profiles

| Profile | Used by | Database | Email |
| --- | --- | --- | --- |
| `local` | Bruna's laptop | Postgres from `local/docker-compose.yaml` (defaults in the file) | Mailpit (defaults in the file) |
| `dev` | Render dev service | env vars | env vars (Resend), no defaults: startup fails if they are missing |
| `prod` | Render prod service | env vars | env vars (Resend), no defaults: startup fails if they are missing |

`prod` also exposes only `health` through Actuator, logs no SQL, logs at `WARN`/`INFO`, and never
puts stack traces or messages in error responses.

## Variables

"Required" means the app does not start without it. `—` means not used in that environment.
"Render" means that variable on that service in that Render workspace (dev: the dev workspace,
prod: the prod workspace).

| Variable | App | Where it's set | Local | Dev | Prod | Secret? |
| --- | --- | --- | --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Backend | Render: backend service. Locally: `-Dspring.profiles.active=local` (the IntelliJ run config sets it) | `local` | `dev` | `prod` | No |
| `POSTGRES_HOST` | Backend | `application-local.yaml` default; Render: backend service | `localhost` (default) | required | required | No |
| `POSTGRES_PORT` | Backend | file default `5432`; Render only to override | `5432` (default) | `5432` (default) | `5432` (default) | No |
| `POSTGRES_DB` | Backend | `application-local.yaml` default; Render: backend service | `booking_system` (default) | required | required | No |
| `POSTGRES_USER` | Backend | `application-local.yaml` default; Render: backend service | `booking_user` (default) | required | required | No |
| `POSTGRES_PASSWORD` | Backend | `application-local.yaml` default (the throwaway password in `local/docker-compose.yaml`); Render: backend service | `postgres` (default) | required | required | secret |
| `JWT_SECRET` | Backend | Render: backend service. Locally: your shell or run config | required (any base64 256-bit key) | required | required | secret |
| `EMAIL_ADDRESS` | Backend | `application-local.yaml` default; Render: backend service | `bookings@localhost` (default) | required | required | No |
| `EMAIL_PASSWORD` | Backend | `application-local.yaml` default; Render: backend service (Resend API key) | `mailpit` (default, ignored by Mailpit) | required | required | secret |
| `EMAIL_SMTP_SERVER` | Backend | `application-local.yaml` default; Render: backend service | `localhost` (default) | required (`smtp.resend.com`) | required (`smtp.resend.com`) | No |
| `EMAIL_SMTP_PORT` | Backend | `application-local.yaml` default; Render: backend service | `1025` (default) | required (`465`) | required (`465`) | No |
| `EMAIL_SMTP_TRANSPORT_STRATEGY` | Backend | `application-local.yaml` default; Render: backend service | `SMTP` (default) | required (`SMTPS`) | required (`SMTPS`) | No |
| `EMAIL_SMTP_USERNAME` | Backend | Render: backend service. Blank means the value of `EMAIL_ADDRESS` | blank | `resend` | `resend` | No |
| `EMAIL_REPLY_TO` | Backend | Render: backend service | blank | optional | optional | No |
| `ALLOWED_ORIGINS` | Backend | file default `http://localhost:3000` (`local`, `dev`); Render: backend service | `http://localhost:3000` (default) | the dev UI origin, plus PR previews if wanted | required: the prod UI origin only | No |
| `PASSWORD_RESET_URL` | Backend | `application-local.yaml` default; Render: backend service | `http://localhost:3000/reset-password` (default) | required: the dev UI's `/reset-password` | required: the prod UI's `/reset-password` | No |
| `ADMIN_BOOTSTRAP_EMAIL` | Backend | Render: backend service | optional | optional | set for the first deploy, then remove | No |
| `ADMIN_BOOTSTRAP_PASSWORD` | Backend | Render: backend service | optional | optional | set for the first deploy, then remove | secret |
| `JAVA_TOOL_OPTIONS` (JVM memory, e.g. `-Xmx384m`) | Backend | Render: backend service. Read by the JVM itself, not by the app; the `Dockerfile` sets no memory options | unset | set to fit the plan | set to fit the plan | No |
| `SENTRY_DSN` | Backend | Render: backend service. **Not read by the code yet**; added by the Sentry ticket | — | planned | planned | secret (treat as one) |
| `SENTRY_ENVIRONMENT` | Backend | Render: backend service. **Not read by the code yet** | — | planned (`dev`) | planned (`prod`) | No |
| `NEXT_PUBLIC_API_URL` | UI | Render: UI service, at build time. Locally `.env.local` (`.env.dev` has the hosted dev value) | `http://localhost:8080` | dev backend URL | prod backend URL | No (public) |
| `NEXT_PUBLIC_SENTRY_DSN` | UI | Render: UI service, at build time. **Not read by the code yet** | — | planned | planned | No (public, ships to the browser) |
| `SENTRY_AUTH_TOKEN` | UI | Render: UI service, build environment only, for source-map upload. **Not read by the code yet** | — | planned | planned | secret |
| `CI` | UI | Set by GitHub Actions; read by `playwright.config.ts` | — | — | — | No |
| `DOCKER_USERNAME` | Backend CI | GitHub secret, booking-system repo (`push_master_docker.yml`) | — | used by CI | used by CI | secret |
| `DOCKER_PASSWORD` | Backend CI | GitHub secret, booking-system repo (`push_master_docker.yml`) | — | used by CI | used by CI | secret |
| Deploy hooks (names set by the deployment ticket) | CI | GitHub secrets | — | one per service | one per service | secret |
| `PROD_DATABASE_URL` | Backup job | GitHub secret. **Not used by any workflow yet** | — | — | planned | secret |
| `BACKUP_AGE_PUBLIC_KEY` | Backup job | GitHub secret. **Not used by any workflow yet** | — | — | planned | No (public key), but keep with the other backup secrets |

The `local` profile's defaults are only for the Docker Compose containers, which are reachable only
from your own machine.

## Keeping this table complete

`ProfileConfigTest` fails if an `application*.yaml` file uses a `${VARIABLE}` that is not in this
file, or if a password, secret, key or token has a literal value in those files. When you add a
variable, add a row here and to `.env.example`. In the UI, grep for `process.env`.
