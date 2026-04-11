# Feedback: local-mvp-auth

Date: 2026-04-11
Scope: IAM auth endpoint restore for deployed UI flow via gateway.

## Implemented
- Added local MVP auth endpoints in `iam-service`:
  - `POST /api/iam/auth/login`
  - `POST /api/iam/auth/refresh`
  - `POST /api/iam/auth/logout`
- Implemented in-memory token/session behavior for local development:
  - login issues placeholder access + refresh token
  - refresh rotates refresh token and returns new token response
  - logout invalidates known refresh token for matching tenant
- Preserved package root under `com.avira.iamservice...`.
- Added/updated OpenAPI schema contract for auth endpoints and token payload.
- Added controller unit tests for success and bad-request behavior.

## Gateway Reachability
- Verified Kong config already contains route prefix `/api/iam/auth` for `iam-service`.
- No Kong config change required.

## Test Result
- Command: `iam-service\\mvnw.cmd test`
- Result: `BUILD SUCCESS`
- Summary: `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0`

## Risks / Follow-up
- Auth tokens are placeholder, unsigned, and in-memory only; restart clears sessions.
- This implementation is for local MVP unblock only and should be replaced by Keycloak-backed flow for non-local environments.
