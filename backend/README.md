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
# Optional dev administrator: configure both the email and mobile; never use a shared demo identity.
$env:ADMIN_MOBILE = '+91YOUR_NUMBER'
$env:ADMIN_EMAIL = 'you@example.com'
mvn spring-boot:run
```

For production, also set `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS`, and `NOTIFICATION_EMAIL_FROM` in the environment or `.env`. Keep `.env` private; the checked-in example contains blank secret fields.

Omit `ADMIN_MOBILE` and `ADMIN_EMAIL` for user-only development. When configured together under `dev`, startup creates/promotes that account to ADMIN. Admin sign-in uses the configured email OTP. The seed never runs without the dev profile.

Email OTP is the only sign-in method. In the development profile, email content is logged locally only when SMTP is not configured. Never enable `dev` in production.

## Email OTP

`POST /api/v1/auth/email-otp/request` sends a one-time code to an address. After code verification, new accounts must submit their name and mobile number to `/api/v1/auth/email-otp/register` before receiving a session. Existing complete accounts sign in after email verification.

Keep `JWT_SECRET` stable across restarts and supply it through your secrets manager. It must contain at least 32 bytes. Configure `FRONTEND_ORIGINS` as comma-separated exact allowed origins. Create the first production administrator through your controlled database/deployment procedure; the development seed is disabled outside `dev`.

## API

| Method | Endpoint | Body / access |
| --- | --- | --- |
| POST | `/api/v1/auth/email-otp/request` | `{email}`; public |
| POST | `/api/v1/auth/email-otp/verify` | `{email, otp}`; public |
| POST | `/api/v1/auth/email-otp/register` | `{email, name, mobile}` after OTP verification; public |
| GET | `/api/v1/auth/me` | Bearer token |
| PUT | `/api/v1/auth/me` | `{name, email}`; Bearer token |
| GET | `/api/v1/admin/users?page=0&size=20` | ADMIN; paginated `content` |
| GET | `/api/v1/admin/users/{id}` | ADMIN |
| PATCH | `/api/v1/admin/users/{id}/role` | `{role: "USER" or "ADMIN"}`; ADMIN |
| PATCH | `/api/v1/admin/users/{id}/status` | `{enabled: true or false}`; ADMIN |

Existing verified email accounts receive `{registrationRequired: false, user, accessToken, tokenType}`. New email addresses receive `{registrationRequired: true}` after code verification and must provide a name and mobile number before registration completes. Emails are lowercased and unique.

Mobile defaults to +91 with 10 national digits; configure `MOBILE_COUNTRY_CODE` and `MOBILE_NATIONAL_LENGTH` together if changing country, and update the frontend country label/validation. Spaces, parentheses and hyphens are normalized. Email is the required sign-in identifier. Mobile is collected for new profiles and normalized on registration.

`OTP_EXPIRATION_SECONDS=300`, `OTP_RESEND_COOLDOWN_SECONDS=30`, `OTP_MAX_ATTEMPTS=5`, `OTP_MAX_REQUESTS_PER_HOUR=10`, `JWT_EXPIRATION_MS=3600000` are configurable. OTP request response includes `resendAfterSeconds` for the UI. Rate limits are per email address, persisted in the database. Failed guesses persist their attempt count. Only the latest email OTP is accepted. Cleanup retains history longer than the hourly request window.

JWTs are signed with HS256 and validated for issuer, signature and expiry. The current database account status and role are checked on every protected request, so deactivation and demotion affect existing tokens. Logout clears the browser session; access tokens expire after the configured lifetime. There is no refresh-token or server-side logout endpoint in this scope.

Use edge-level IP/global request limits for public email OTP endpoints.

## Tests

`mvn test` runs the integration tests against `localhost:3306/movie_booking_test` using the MySQL Flyway migrations. The test profile uses the local `root` account and reads its password from `DB_PASSWORD` (or the backend `.env` file); it never uses the application database. The test database must already exist. Tests do not use H2, Docker, or Testcontainers.

The application database URL remains configurable for deployment with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`. `DB_URL` can also be set to override the composed URL. Production defaults to the separate `movie_booking` database.

`mvn package` produces the executable jar in `target`.
