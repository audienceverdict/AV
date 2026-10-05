# Authentication backend

Java 21, Maven, Spring Boot 3.4.13, MySQL 8. Flyway owns schema changes; Hibernate uses `ddl-auto=validate`.

## Local configuration (PowerShell)

Use a dedicated local MySQL user with permission to create the database. The backend JDBC URL includes `createDatabaseIfNotExist=true`, so MySQL creates `movie_booking` automatically on first startup when the user has enough privileges.

```sql
CREATE USER 'movie_user'@'localhost' IDENTIFIED BY '<choose-a-local-password>';
GRANT ALL PRIVILEGES ON *.* TO 'movie_user'@'localhost';
FLUSH PRIVILEGES;
```

Local MySQL connection:

```text
Host: localhost
Port: 3306
Database: movie_booking
Username: movie_user
Password: your locally configured database password
JDBC URL: jdbc:mysql://localhost:3306/movie_booking?createDatabaseIfNotExist=true
```

Copy the example file to `.env` in this directory and fill in your local database credentials. Spring Boot imports this file automatically when the application starts from `backend`; operating-system environment variables still take precedence.

```powershell
Copy-Item .env.example .env
$env:DB_URL = 'jdbc:mysql://localhost:3306/movie_booking?createDatabaseIfNotExist=true'
$env:DB_USERNAME = 'movie_user'
$env:DB_PASSWORD = '<your-local-database-password>'
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
$env:SPRING_PROFILES_ACTIVE = 'dev'
# Optional: your own mobile number; never use a shared demo identity.
$env:ADMIN_MOBILE = '+91YOUR_NUMBER'
mvn spring-boot:run
```

For production, also set `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS`, and `NOTIFICATION_EMAIL_FROM` in the environment or `.env`. Keep `.env` private; the checked-in example contains blank secret fields.

Omit `ADMIN_MOBILE` for user-only development. When configured under `dev`, startup creates/promotes that account to ADMIN. Every login still requires its generated OTP. The seed never runs without the dev profile.

The dev SMS provider logs random OTPs to the local backend console. Never enable `dev` in production. Codes are not returned by API responses. Normal login does not use passwords. BCrypt is used only to hash OTPs.

## Production SMS

Without `dev`, startup requires `SMS_WEBHOOK_URL` (HTTPS) and `SMS_API_TOKEN`. The configured SMS adapter receives a POST with `Authorization: Bearer <token>` and JSON `{ "mobile": "+91...", "message": "Your Audience Verdict verification code is ..." }`. Connect this endpoint to your SMS delivery service, or implement `SmsProvider` for a vendor. Non-2xx delivery responses fail the OTP request and roll back the challenge. No vendor credentials or real SMS service are supplied by this project.

Keep `JWT_SECRET` stable across restarts and supply it through your secrets manager. It must contain at least 32 bytes. Configure `FRONTEND_ORIGINS` as comma-separated exact allowed origins. Create the first production administrator through your controlled database/deployment procedure; the development seed is disabled outside `dev`.

## API

| Method | Endpoint | Body / access |
| --- | --- | --- |
| POST | `/api/v1/auth/otp/request` | `{mobile}`; public |
| POST | `/api/v1/auth/otp/verify` | `{mobile, otp}`; public |
| GET | `/api/v1/auth/me` | Bearer token |
| PUT | `/api/v1/auth/me` | `{name, email}`; Bearer token |
| GET | `/api/v1/admin/users?page=0&size=20` | ADMIN; paginated `content` |
| GET | `/api/v1/admin/users/{id}` | ADMIN |
| PATCH | `/api/v1/admin/users/{id}/role` | `{role: "USER" or "ADMIN"}`; ADMIN |
| PATCH | `/api/v1/admin/users/{id}/status` | `{enabled: true or false}`; ADMIN |

Verification returns `{user, accessToken, tokenType}`. User fields: string UUID `id`, `name`, normalized `mobile`, nullable `email`, `role`, `enabled`, ISO `createdAt`. Profile edits reject unknown fields, including role/mobile/account status. Email is optional, lowercased and unique when present. New users receive `USER`, enabled, and the default display name `Movie lover`.

Mobile defaults to +91 with 10 national digits; configure `MOBILE_COUNTRY_CODE` and `MOBILE_NATIONAL_LENGTH` together if changing country, and update the frontend country label/validation. Spaces, parentheses and hyphens are normalized. The frontend maps nullable email to an empty string for form fields.

`OTP_EXPIRATION_SECONDS=300`, `OTP_RESEND_COOLDOWN_SECONDS=30`, `OTP_MAX_ATTEMPTS=5`, `OTP_MAX_REQUESTS_PER_HOUR=10`, `JWT_EXPIRATION_MS=3600000` are configurable. OTP request response includes `resendAfterSeconds` for the UI. Rate limits are per normalized mobile, persisted in the database. Database lock stripes serialize OTP operations across instances, including first registration; failed guesses persist their attempt count. Only the latest OTP is accepted. Cleanup retains history longer than the hourly request window.

JWTs are signed with HS256 and validated for issuer, signature and expiry. The current database account status and role are checked on every protected request, so deactivation and demotion affect existing tokens. Logout clears the browser session; access tokens expire after the configured lifetime. There is no refresh-token or server-side logout endpoint in this scope.

Use edge-level IP/global request limits when deploying public SMS endpoints; per-mobile limits alone do not cap total SMS spend across many numbers.

## Tests

`mvn test` runs endpoint, persistence, concurrency, OTP, profile, JWT, CORS and authorization checks with H2. `mvn package` produces the executable jar in `target`.
