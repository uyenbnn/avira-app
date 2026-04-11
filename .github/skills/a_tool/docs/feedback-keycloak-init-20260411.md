# Feedback - keycloak-init-20260411

## Implemented
- Replaced in-memory Keycloak provisioning bean with `RestKeycloakRealmProvisioningService` using Keycloak Admin REST API.
- Added startup bootstrap flow with readiness wait + retry/backoff:
  - `KeycloakReadinessWatcher`
  - `SharedRealmBootstrap`
  - `SharedRealmInitStatus`
- Extended `IamRealmProperties` for shared realm/client naming and added tenant backend client pattern helper.
- Added typed init properties (`KeycloakInitProperties`) and enabled configuration binding in `IamServiceApplication`.
- Updated `application-init.yml` secret key usage to `KEYCLOAK_PLATFORM_CLIENT_SECRET`.
- Updated OpenAPI text for tenant backend client naming.
- Updated resolver unit test for expanded `IamRealmProperties` constructor.

## Validation
- Ran iam-service tests:
  - `AuthControllerTest`
  - `RealmProvisioningControllerTest`
  - `SharedOrDedicatedRealmResolverTest`
- Result: 17 passed, 0 failed.

## Notes / Follow-up
- Current Keycloak Admin token acquisition uses admin password grant (`admin-cli`) from `iam.init.keycloak.*` properties.
- Service-account auth using `saas-backend` client credentials is still a follow-up to fully align with the long-term trust-boundary target.
- `AuthService` has an existing static-analysis warning (duplicated literal) unrelated to this ticket.
