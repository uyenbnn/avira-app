# Changelog

## 2026-03-22

### Phase 1 - Authentication Service Entry Points
- Added `authentication-service` API endpoints: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, and `POST /api/v1/auth/refresh-token`.
- Introduced lightweight orchestration in `authentication-service` to call Keycloak token endpoint for login and refresh.
- Kept register flow compatible by delegating to the current `user-service` register endpoint.

### Phase 1.1 - Stateless Auth Service Hardening
- Marked `authentication-service` as stateless by excluding JDBC/JPA auto-config through `excludeName` in `AuthenticationServiceApplication`.
- Kept module dependencies aligned with no-database ownership.

### Phase 2 - Identity Ownership Cutover (planned)
- Move register identity provisioning fully into `authentication-service` (no pass-through to `user-service`).
- Keep `user-service` focused on profile/domain lifecycle only.

### Phase 2.1 - Legacy User-Service Auth Compatibility Signal
- Added migration headers on `user-service` legacy auth endpoints (`/api/v1/auth/register`, `/api/v1/auth/login`) to signal deprecation and direct clients to `authentication-service`.
- Kept behavior backward compatible while migration is in progress.

### Phase 2.2 - Refresh Token Ownership + Legacy Endpoint
- Kept `authentication-service` as the owner of `POST /api/v1/auth/refresh-token`.
- Added a legacy compatibility endpoint in `user-service` at `POST /api/v1/auth/refresh-token` that forwards to `authentication-service` and returns migration headers.
- No data migration applied in this phase.

### Phase 2.3 - Remove Legacy Refresh Endpoint + Common Lib Seed
- Removed legacy `POST /api/v1/auth/refresh-token` from `user-service`; refresh-token ownership remains in `authentication-service`.
- Added new `common-lib` module and registered it in the root reactor.
- Introduced reusable auth constants in `common-lib` for API paths and migration headers; wired `user-service` and `authentication-service` to use them.
- Moved hardcoded auth client API path usage to shared constants (kept base URLs configurable).

### Phase 3 - Initialization Ownership Cutover (planned)
- Move Keycloak realm/bootstrap responsibilities from `user-service` to `application-initialization-service`.
- Keep bootstrap logic behind explicit init controls.

### Phase 3.1 - Initialization Service Bootstrap Seed
- Added Keycloak bootstrap flow in `application-initialization-service` to initialize the default realm/tenant, default auth client, and admin client.
- Added seed user creation for `anonymous` user and default admin user with idempotent checks.
- Added `POST /init` endpoint to trigger initialization explicitly, with optional startup auto-run flag.

### Phase 3.2 - Admin Client Permission Hardening
- Hardened `application-initialization-service` admin client bootstrap to enforce confidential/service-account settings idempotently.
- Added realm-management role seeding for the admin client service account (`view-users`, `query-users`, `manage-users`, `view-realm` by default).
- Added optional admin client secret and role list configuration via `application.properties` for environment-specific bootstrap control.

### Phase 3.3 - Direct Keycloak User Registration in Authentication Service
- `authentication-service` now creates users directly in Keycloak via the Admin API (no longer delegates to `user-service`).
- Added `KeycloakUserRegistrationService` to handle user creation, password assignment, and default role assignment (`USER`).
- Added `KeycloakAdminConfig` to provide a Keycloak admin client bean in `authentication-service`.
- Updated `RegisterRequest` to include `username`, `email`, `password`, `firstName`, `lastName` (removed `phone`).
- Updated `UserResponse` to reflect Keycloak identity fields: `id`, `username`, `email`, `firstName`, `lastName`.
- Added `UserRoles` constants (`USER`, `ADMIN`, `SELLER`, `BUYER`, `ANONYMOUS`) to `common-lib` for shared role name references.
- Removed `UserRegistrationWebClient` bean from `authentication-service` — no longer used.
- Updated `application.properties` with Keycloak admin connection config (`keycloak.admin.*` and `keycloak.auth.realm`).

### Phase 3.4 - Common-Lib Integration in Application Initialization Service
- Added `common-lib` dependency to `application-initialization-service`.
- Replaced hardcoded role name strings in `KeycloakInitializationService` with `UserRoles` constants (`UserRoles.USER`, `UserRoles.ADMIN`, `UserRoles.SELLER`, `UserRoles.BUYER`, `UserRoles.ANONYMOUS`).

### Phase 3.5 - Authentication Role Update Endpoint
- Added `PUT /auth/users/{userId}/roles` in `authentication-service` to update Keycloak realm roles for a user.
- Added `UpdateUserRolesRequest` and `UserRolesResponse` DTOs for role update contracts.
- Added `KeycloakUserRoleService` to validate requested roles against `UserRoles.ALL` and synchronize managed roles in Keycloak.
- Added unit tests for orchestration/controller flow and role-sync behavior (`add`, `remove`, unchanged, invalid role). 

### Phase 4 - Cleanup and Compatibility (planned)
- Remove legacy identity endpoints from `user-service` after migration validation.
- Align contracts, tests, and docs with the final service boundaries.
