## Prerequisites
- java 21
- maven
- docker

## Running Steps

1. Configure Database Password:

The database password must be set and changed in both the _docker-compose.yaml_ and _application-local.yml_ files.

If you change the `POSTGRES_PASSWORD` in _docker-compose.yaml_, you must also update the `spring.datasource.password` in _application-local.yml_ to match.

2. Start the Database and Mailpit:
The _docker-compose.yaml_ file is located in the _local_ folder. Navigate to this folder.
Run the following command to start the PostgreSQL database and Mailpit containers:

```bash
cd ./local
docker-compose up -d
```

Mailpit is a fake SMTP server for local dev — the app sends real emails to it instead of a
real provider, and you view them at http://localhost:8025 instead of an inbox. No real email
account or credentials needed locally. Deployed environments (dev/prod) still use a real
Gmail account via `EMAIL_ADDRESS`/`EMAIL_PASSWORD`.

3. Run the Application:

Use the provided IntelliJ run configuration file _BookingSystemApplication.run.xml_ to run the application.

Alternatively, you can build and run the application using Maven from the command line:

```bash
mvn clean install

java -Dspring.profiles.active=local -jar target/booking-system-image.jar.
```

## Admin Bootstrap

On startup, if no admin user exists yet, the app creates one from the
`ADMIN_BOOTSTRAP_EMAIL` / `ADMIN_BOOTSTRAP_PASSWORD` environment variables (see
`.env.example`). This only ever fires once — once an admin exists, further password changes
go through the normal reset/change-password flow, not this mechanism.

If both are left unset, the app still starts, but logs a warning and has no usable admin
account. For local dev, set them in `application-local.yaml` (git-ignored) if you need a
working admin locally; otherwise it's safe to leave blank.

## CORS Allowed Origins

The frontend origins allowed to call the API are set with `ALLOWED_ORIGINS` (property
`allowed.origins`): a comma-separated list, e.g.

```
ALLOWED_ORIGINS=http://localhost:3000,https://booking-ui-d6mq.onrender.com,https://booking-ui-d6mq-pr-*.onrender.com
```

Entries may contain `*` wildcards (useful for per-PR preview deployments). Whitespace around
entries is trimmed and empty entries are ignored. A bare `*` is rejected and the app fails to
start. If unset, the `dev` profile defaults to `http://localhost:3000`.

## Asking Claude to act on review comments

Comment `@claude` followed by what you want on a pull request, a review comment or a review. Claude (via `.github/workflows/claude.yml`) pushes changes to that PR's branch and replies. Only comments from BBGMoura are acted on, and it needs the `CLAUDE_CODE_OAUTH_TOKEN`, `WONDERBOT_APP_ID` and `WONDERBOT_APP_PRIVATE_KEY` repo secrets.
