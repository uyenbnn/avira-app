# Feedback: local-mvp

Date: 2026-04-11
Scope: Final deployed UI validation against local MVP backend via gateway `http://localhost:10001`.

## What worked
- UI dev server starts and serves correctly on `http://127.0.0.1:4200`.
- SPA deep links return `200` for `/auth/login` and `/business/apps`.
- Platform tenant creation endpoint is reachable and returns `201` with expected fields.

## API usability gaps
- IAM login route mismatch:
  - UI and integration tests target `POST /api/iam/auth/login`.
  - Gateway currently returns `404 Not Found` for this route.
  - Impact: business login flow cannot be validated end-to-end from UI.
- Platform app endpoints require validated tenant token context:
  - `POST /api/platform/tenants/{tenantId}/applications` returns `403` with `Missing tenant context from validated token` when called without token context.
  - `GET /api/platform/tenants/{tenantId}/applications` also returns `403` with the same message.
  - Impact: create-app and list-app stages are blocked without a successful IAM login/token issuance path.

## Recommendations
- Restore or expose the expected IAM login path `POST /api/iam/auth/login` through Kong route config and backend controller mapping.
- Document required token claims/tenant context for platform app endpoints in UI integration notes.
- Add a smoke script that verifies IAM login and platform create-app in one flow before declaring local MVP ready.
