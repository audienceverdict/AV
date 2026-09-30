# Audience Verdict frontend

React, TypeScript, Vite. Run `npm install`, then `npm run dev`; on PowerShell use `npm.cmd` if script execution is disabled. Start the backend as described in [../backend/README.md](../backend/README.md).

Authentication uses real mobile OTP APIs, JWTs, `/auth/me`, profile updates and backend-authorized admin user management. The token lives in sessionStorage for reloads within the tab; the user/role are loaded from the backend. Invalid/expired tokens clear the session. There is no fixed OTP or hardcoded admin number. Use your generated code from the backend console in development, or SMS with a configured production provider.

`src/services/auth.ts` implements the API adapter. `src/context/AppContext.tsx` restores authentication before protected pages render. Vite proxies `/api` to port 8080. Production hosting must route `/api` to the backend and provide SPA fallback for other paths.

`npm test` and `npm run build` run validation.

Movies, theatres, layouts, shows, reviews, bookings and settings still use localStorage demo repositories in `src/services/api.ts`. Payments, booking notifications and ticket QR visuals remain simulations. Existing browser demo users/bookings are not migrated into backend accounts. Real auth UUIDs are separate from seeded demo identities. The frontend admin catalogue controls are not a substitute for backend authorization on those future modules.
