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


# Keycloak Initialization (Implemented)

The following Keycloak realms, clients, and users are auto-provisioned for local and test environments:

## Realms Created
- `saas`
- `avira-platform`

## Clients (per realm)

**saas**
- `saas-backend` (confidential, service account enabled)
- `saas-console` (public, PKCE, for UI)

**avira-platform**
- `avira-platform-backend` (confidential, service account enabled)
- `avira-platform-public` (public, PKCE, for UI)

## Default Users

**saas**
- `saas-admin` (enabled, admin role)
- `anonymous-saas` (anonymous role)

**avira-platform**
- `anonymous-platform` (disabled, anonymous role)

## Local Deploy & Verification

1. Deploy Keycloak with realm templates:
	- See: [`deploy/keycloak/realm-templates/saas-realm.json`](../../deploy/keycloak/realm-templates/saas-realm.json)
	- See: [`deploy/keycloak/realm-templates/avira-platform-realm.json`](../../deploy/keycloak/realm-templates/avira-platform-realm.json)
2. Start `iam-service` and supporting services (see `docker-compose.yml`).
3. Verify realms, clients, and users are present using Keycloak admin UI or API.

## Test Evidence

Integration tests validate Keycloak initialization:
- [integration-tests/node-axios/tests/keycloak-realms-auto-created.integration.js](../../integration-tests/node-axios/tests/keycloak-realms-auto-created.integration.js)
- [integration-tests/node-axios/tests/keycloak-clients-provisioned.integration.js](../../integration-tests/node-axios/tests/keycloak-clients-provisioned.integration.js)
- [integration-tests/node-axios/tests/keycloak-default-users-provisioned.integration.js](../../integration-tests/node-axios/tests/keycloak-default-users-provisioned.integration.js)

This documents the actual implemented and tested Keycloak initialization for local and CI environments.
