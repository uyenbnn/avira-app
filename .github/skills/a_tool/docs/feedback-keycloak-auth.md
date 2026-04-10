# feedback-keycloak-auth

## feedback
- Implemented tenant-aware Keycloak provisioning in iam-service via `KeycloakProvisionService` and startup `InitRunner`.
- Added realm resolution strategy (`SharedOrDedicatedRealmResolver`) and integrated authentication-provider runtime calls for login/refresh/logout/userinfo/introspection.
- Updated auth DTO/controller/service to include tenant context and return application token payload claims.

## improvement
- Move sensitive secrets (backend client secret, admin password) to k8s secrets/vault and avoid default plaintext values.
- Add dedicated integration tests against a live Keycloak container in `integration-tests/node-axios` for token and provisioning flows.
- Add retry/backoff around Keycloak availability for startup provisioning in non-dev environments.

## next_step
- Add a provisioning endpoint in iam-service for explicit tenant onboarding (called by platform-service tenant creation flow).
- Add stronger claim validation (`tenant_id`, audience, issuer) in `AuthenticationService` using JWKS signature checks.
- Add deploy manifest updates under `deploy/k3s` for Keycloak readiness probes and iam-service startup ordering.

## retry-2026-04-11
### feedback
- Verified iam-service init module contains required `RealmResolver`, `SharedOrDedicatedRealmResolver`, `KeycloakProvisionService`, `InitRunner`, and `application-init.yml` properties for Keycloak bootstrap.
- Confirmed authentication integration is wired through `AuthenticationProvider` and `KeycloakAuthenticationProvider` with tenant-aware realm resolution for login, refresh, logout, userinfo, introspection, and client-credentials token exchange.
- Added `InitRunnerTest` to verify startup behavior when provisioning is enabled and disabled.

### improvement
- Add explicit unit tests for `KeycloakProvisionService` with mocked admin REST interactions (token retrieval, realm create-if-missing, client upsert, role upsert).
- Extract `appId` from trusted JWT or gateway headers in auth endpoints to avoid relying on request-body ownership.

### next_step
- If a standalone `authentication-service` module is required, add it to the repository structure and move runtime auth package from iam-service with preserved contracts.
