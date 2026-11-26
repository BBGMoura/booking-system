## Prerequisites
- java 21
- maven
- docker

## Running Steps

1. Configure the environment:

The `local` profile (_application-local.yaml_) already points at the database and Mailpit from
_local/docker-compose.yaml_, so the only variable you must set is `JWT_SECRET`
(`openssl rand -base64 32`). If you change `POSTGRES_PASSWORD` in _docker-compose.yaml_, set the
same `POSTGRES_PASSWORD` in your environment. See [CONFIG.md](CONFIG.md) for every variable and
`.env.example` for a template.

2. Start the Database and Mailpit:
The _docker-compose.yaml_ file is located in the _local_ folder. Navigate to this folder.
Run the following command to start the PostgreSQL database and Mailpit containers:

```bash
cd ./local
docker-compose up -d
```

Mailpit is a fake SMTP server for local dev — the app sends real emails to it instead of a
real provider, and you view them at http://localhost:8025 instead of an inbox. No real email
account or credentials needed locally. Deployed environments (`dev` and `prod`) have no email defaults: they must be given an SMTP provider
(Resend) through environment variables, or the app will not start (see [Email](#email)).

3. Run the Application:

Use the provided IntelliJ run configuration file _BookingSystemApplication.run.xml_ (it uses the
`local` profile) to run the application.

Alternatively, you can build and run the application using Maven from the command line:

```bash
./mvnw clean install

./mvnw -pl booking-system-app spring-boot:run -Dspring-boot.run.profiles=local
```

## Email

The app sends email over SMTP, so the provider is chosen by environment variables alone.
On `dev` and `prod` all of `EMAIL_ADDRESS`, `EMAIL_PASSWORD`, `EMAIL_SMTP_SERVER`, `EMAIL_SMTP_PORT` and
`EMAIL_SMTP_TRANSPORT_STRATEGY` must be set. On `local` they default to Mailpit.

| Variable | Required | Default | Purpose |
| --- | --- | --- | --- |
| `EMAIL_ADDRESS` | Yes | – | The From address on every email |
| `EMAIL_PASSWORD` | Yes | – | The SMTP password (a Resend API key) |
| `EMAIL_SMTP_SERVER` | Yes on dev/prod | `localhost` on local | SMTP host |
| `EMAIL_SMTP_PORT` | Yes on dev/prod | `1025` on local | SMTP port |
| `EMAIL_SMTP_TRANSPORT_STRATEGY` | Yes on dev/prod | `SMTP` on local | `SMTP`, `SMTP_TLS` or `SMTPS` |
| `EMAIL_SMTP_USERNAME` | No | the value of `EMAIL_ADDRESS` | SMTP login name |
| `EMAIL_REPLY_TO` | No | none | When set, the Reply-To on every email |

Resend example:

```bash
EMAIL_ADDRESS=bookings@acsdance.uk
EMAIL_PASSWORD=<Resend API key with sending access only>
EMAIL_SMTP_SERVER=smtp.resend.com
EMAIL_SMTP_PORT=465
EMAIL_SMTP_TRANSPORT_STRATEGY=SMTPS
EMAIL_SMTP_USERNAME=resend
EMAIL_REPLY_TO=<the ACS mailbox>
```

## Admin Bootstrap

On startup, if no admin user exists yet, the app creates one from the
`ADMIN_BOOTSTRAP_EMAIL` / `ADMIN_BOOTSTRAP_PASSWORD` environment variables (see
`.env.example`). This only ever fires once — once an admin exists, further password changes
go through the normal reset/change-password flow, not this mechanism.

If both are left unset, the app still starts, but logs a warning and has no usable admin
account. For local dev, set them in your environment if you need a working admin locally;
otherwise it's safe to leave blank.

## CORS Allowed Origins

The frontend origins allowed to call the API are set with `ALLOWED_ORIGINS` (property
`allowed.origins`): a comma-separated list, e.g.

```
ALLOWED_ORIGINS=http://localhost:3000,https://booking-ui-d6mq.onrender.com,https://booking-ui-d6mq-pr-*.onrender.com
```

Entries may contain `*` wildcards (useful for per-PR preview deployments). Whitespace around
entries is trimmed and empty entries are ignored. A bare `*` is rejected and the app fails to
start. If unset, the `local` and `dev` profiles default to `http://localhost:3000`; `prod` requires it.

## Configuration

Which setting lives in a file, in Render or in GitHub, for each environment: see [CONFIG.md](CONFIG.md).
