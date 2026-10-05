# Audience Verdict

- `frontend/`: React + TypeScript application, connected to the authentication API.
- `backend/`: Spring Boot authentication and user management, MySQL and Flyway.

Authentication is implemented here. Movie catalogue, theatres, seats, shows, bookings, payments, reviews, and booking notifications remain browser demo features. They are not secured or persisted by this backend yet.

## Start locally

1. Set up MySQL and the environment variables in [backend/README.md](backend/README.md).
2. In `backend`, run `mvn spring-boot:run` with `SPRING_PROFILES_ACTIVE=dev`.
3. In a second terminal: `cd frontend`, `npm install`, then `npm run dev` (use `npm.cmd` in restricted PowerShell).
4. Open `http://localhost:5173`. Request an OTP; in development, read the generated code from the backend console. There is no fixed demo code.

The Vite development server proxies `/api` to `http://localhost:8080`. For deployment, route `/api` to the backend and all other routes to the frontend with SPA fallback. Use HTTPS.

## Checks

```sh
cd frontend
npm test
npm run build
cd ../backend
mvn test
```

Backend tests exercise the real Spring Security chain, Flyway migrations and repositories with H2 in MySQL mode. The same integration suite runs on MySQL 8 through Testcontainers when Docker is available; it is explicitly skipped otherwise. H2 passing alone does not verify MySQL deployment.
"# AV" 
"# AV" 
