# Frontend Feedback: 20260411 SaaS MVP

## Implemented UI/API Wiring

- Auth login uses POST /api/iam/auth/login
- Auth refresh wiring added to service for POST /api/iam/auth/refresh
- Business workflow uses:
  - POST /api/platform/tenants
  - POST /api/platform/tenants/{tenantId}/applications
  - GET /api/platform/tenants/{tenantId}/applications

## API Usability Gaps Observed

1. Login request shape mismatch between architecture flow and OpenAPI
- Architecture artifact expects email/password.
- IAM OpenAPI currently requires tenantId/username/password (appId optional).
- Frontend implemented against OpenAPI shape to avoid runtime mismatch.

2. No platform OpenAPI spec found in repository
- Platform contract was taken from architecture markdown only.
- Frontend can call documented endpoints, but validation of exact error schema and optional fields is limited.

3. Callback token contract not formally documented
- Callback route handling in frontend expects query params accessToken and refreshToken.
- There is no explicit backend/API contract for this callback payload in repository docs.

## Recommendation

- Publish a single source of truth OpenAPI for platform-service and align IAM login schema across architecture and OpenAPI.

## Backend Feedback Update (2026-04-11)

### Implemented MVP Skeleton

- Added IAM realm resolution/provisioning skeleton endpoints and service contracts under `com.avira.iamservice...`.
- Added application-service auth strategy router and token exchange endpoint under `com.avira.applicationservice...`.
- Added platform-service tenant/application MVP endpoints with ownership checks under `com.avira.platformservice...`.
- Added OpenAPI resources for application-service and platform-service; expanded IAM OpenAPI with provisioning and realm-config endpoints.

### Constraint Compliance

- SHARED_REALM enforced for MVP provisioning/config APIs.
- Keycloak Admin API ownership remains in iam-service boundary via `KeycloakRealmProvisioningService` abstraction.
- Ownership-sensitive paths derive tenant context from validated-token header placeholder (`X-Tenant-Id`) rather than request body.

### Follow-up Risks

- Current implementation uses in-memory stores and placeholder token-claim headers for compile-first MVP; persistence and real JWT claim extraction must be wired next.
- IAM auth login/refresh/logout remain OpenAPI-only in this pass and are not implemented as concrete controllers.
