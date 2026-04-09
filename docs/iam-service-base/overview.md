# IAM Service Base Implementation

This document describes the foundational implementation added to `iam-service`.

## Scope

Implemented base modules:
- authenticationservice
- userservice
- clientservice
- initservice
- roleservice
- permissionservice

## Key AGENTS Alignment

- RealmResolver contract implemented in `clientservice.service.RealmResolver`.
- Shared and dedicated realm routing implemented in `DefaultRealmResolver`.
- Dedicated realm requires explicit approval in tenant realm config upsert.
- Shared realm bootstrap is triggered at startup via `initservice.service.SharedRealmBootstrap`.
- Keycloak Admin integration is kept behind interface `KeycloakRealmProvisioningService` with current no-op implementation.

## Database

Flyway migration:
- `iam-service/src/main/resources/db/migration/V1__create_iam_base_tables.sql`

Tables:
- `tenant_realm_config`
- `platform_user`

## API Surface (Base)

Authentication:
- `POST /api/iam/auth/login`
- `POST /api/iam/auth/refresh`
- `POST /api/iam/auth/logout`

Tenant realm config and resolution:
- `POST /api/iam/client/tenant-realm-configs`
- `GET /api/iam/client/realms/tenants/{tenantId}`

Platform users:
- `POST /api/iam/users`
- `GET /api/iam/users`
- `GET /api/iam/users/{userId}`

Roles:
- `POST /api/iam/roles/users/{userId}`
- `GET /api/iam/roles/users/{userId}`

Permissions:
- `GET /api/iam/permissions/roles/{role}`

## Notes

This is a base scaffold to establish service boundaries and core contracts.
Production integrations still required:
- real Keycloak Admin API provisioning and client/role setup
- token exchange and validation against Keycloak
- persistent role assignments and permission policies
- integration tests against local k3s runtime
