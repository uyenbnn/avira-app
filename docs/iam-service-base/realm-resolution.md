# Realm Resolution & Keycloak Initialization Contract

This document defines the RealmResolver contract, MVP realm strategy, Keycloak initialization algorithm, required clients and users, trust boundaries, and all configuration keys. It is the single authoritative reference for Keycloak initialization and realm routing.

**Artifact:** `docs/iam-service-base/realm-resolution.md`  
**Last Updated:** 2026-04-11  
**Source Plan:** `.github/skills/a_tool/plan/po-plan-20260411-keycloak-init.md`  
**Architect Artifact:** `.github/skills/a_tool/architect/arch-keycloak-init-20260411.md`

---

## 1. MVP Realm Strategy

**Active for MVP:** `SHARED_REALM` only.

| Decision | Value |
|---|---|
| Platform admin realm | `saas` (fixed name, not tenant-derived) |
| Default shared realm | `avira-platform` (from `iam.realm.shared-name`) |
| Identity mode at tenant creation | `SHARED_REALM` (hardcoded for MVP) |
| `DEDICATED_REALM` activation | **Out of scope for MVP.** Interface and data model are scaffolded but cannot be activated without explicit approval gate. |
| Tenant isolation mechanism | `tenant_id` JWT claim + scoped DB queries. NOT by realm. |

### Non-Negotiable MVP Constraints

1. `identityMode` field on `TenantRealmConfig` MUST be set to `SHARED_REALM` for all tenants created in MVP.
2. `dedicatedRealmApproved = false` MUST be enforced at the API level; the field is not user-settable in MVP.
3. `RealmResolver.resolveRealm(tenantId)` MUST be used for every Keycloak operation — no hardcoded realm strings anywhere except the default config property.
4. Realm provisioning (create/delete Keycloak realm) is callable only through `KeycloakRealmProvisioningService` inside `iam-service`. No other service may call Keycloak Admin API.
5. `application-service` validates tokens against Keycloak JWKS endpoint only; it MUST NOT perform any Keycloak Admin API call.

---

## 2. Realm Model & Naming Convention

Two pre-provisioned realms are required at initialization. All names are fixed for MVP.

| Realm | Name | Purpose | Naming Rule |
|---|---|---|---|
| Platform Admin Realm | `saas` | Platform admin authentication, iam-service service accounts, management tooling | Fixed; never derived from tenant data |
| Shared Tenant Realm | `avira-platform` | Business users, end users, all SHARED_REALM tenants | Configured via `iam.realm.shared-name`; default `avira-platform` |
| Dedicated Tenant Realm | `tenant_{tenantId}` | Post-MVP only; per-tenant full isolation | Prefix from `iam.realm.dedicated-prefix`; default `tenant_` |

**Constraints:**
- `saas` realm is NOT a tenant-scoped realm. It MUST NOT appear in `TenantRealmConfig`.
- The shared realm name is always read from config, never hardcoded in service logic.
- Dedicated realm naming uses `tenant_<tenantId>` where `tenantId` is the UUID string.

---

## 3. Required Clients per Realm

### 3.1 `saas` Realm Clients

| Client ID | Type | Grant Types | Purpose |
|---|---|---|---|
| `saas-backend` | Confidential | `client_credentials` | iam-service service account for calling Keycloak Admin API targeting the `saas` realm |
| `saas-console` | Public | `authorization_code` + PKCE | Platform admin UI / management console browser login |

**`saas-backend` required settings:**
- `serviceAccountsEnabled: true`
- `directAccessGrantsEnabled: false`
- `publicClient: false`
- Service account role: `realm-management/realm-admin` (Keycloak built-in, scoped to `saas` realm)
- Client secret: injected via `KEYCLOAK_SAAS_CLIENT_SECRET`; stored in k8s Secret / compose env

**`saas-console` required settings:**
- `publicClient: true`
- `standardFlowEnabled: true`
- `pkceCodeChallengeMethod: S256`
- `redirectUris: ["http://localhost:4200/*", "https://<platform-domain>/*"]`
- `webOrigins: ["+"]`

### 3.2 `avira-platform` Realm Clients

| Client ID | Type | Grant Types | Purpose |
|---|---|---|---|
| `avira-platform-backend` | Confidential | `client_credentials` + `password` | iam-service backend operations (token exchange, user provisioning) against the shared realm |
| `avira-platform-public` | Public | `authorization_code` + PKCE | Browser-based login for business users and end users (saas-ui-app) |

**`avira-platform-backend` required settings:**
- `serviceAccountsEnabled: true`
- `directAccessGrantsEnabled: true` (needed for token exchange in MVP until PKCE is fully wired)
- `publicClient: false`
- Service account roles: `realm-management/manage-users`, `realm-management/view-users`, `realm-management/manage-clients`
- Client secret: injected via `KEYCLOAK_PLATFORM_CLIENT_SECRET`

**`avira-platform-public` required settings:**
- `publicClient: true`
- `standardFlowEnabled: true`
- `pkceCodeChallengeMethod: S256`
- `redirectUris: ["http://localhost:4200/*", "https://<platform-domain>/*"]`
- `webOrigins: ["+"]`

### 3.3 Per-Tenant Client (dynamic, `avira-platform` realm)

Provisioned on-demand by `KeycloakRealmProvisioningService.provisionTenantClient()` when a tenant is first provisioned.

| Client ID pattern | Type | Purpose |
|---|---|---|
| `tenant-{tenantId}-backend` | Confidential | Tenant-scoped backend operations; maps to `TenantClientProvisionResult.keycloakClientId` |

**Rules:**
- Only created if tenant `identityMode = SHARED_REALM` (MVP constraint).
- Provisioning is idempotent: if client already exists, return `ALREADY_EXISTS` status without mutating it.
- Client secret is generated by Keycloak and stored by iam-service for later retrieval.

---

## 4. Required Users per Realm

### 4.1 `saas` Realm Users

| Username | Role(s) | Purpose | Credential Strategy |
|---|---|---|---|
| `saas-admin` | `platform-admin` (custom realm role) | Default platform administrator | Initial password from `KEYCLOAK_SAAS_ADMIN_PASSWORD`; `temporaryPassword: true` (must change on first login) |
| `anonymous-saas` | `anonymous` (custom realm role) | Guest/unauthenticated access sentinel for saas realm flows | Random UUID password set at init; `enabled: false` (not usable for direct login); used for service-level anonymous token grants only |

**`saas-admin` required attributes:**
```
platform_role: PLATFORM_ADMIN
email: <KEYCLOAK_SAAS_ADMIN_EMAIL>
emailVerified: true
```

**`anonymous-saas` required attributes:**
```
platform_role: ANONYMOUS
emailVerified: false
enabled: false
```

### 4.2 `avira-platform` Realm Users

| Username | Role(s) | Purpose | Credential Strategy |
|---|---|---|---|
| `anonymous-platform` | `anonymous` (custom realm role) | Guest access sentinel for shared realm; used to obtain restricted tokens without a real user identity | Random UUID password set at init; `enabled: false` |

**`anonymous-platform` required attributes:**
```
platform_role: ANONYMOUS
emailVerified: false
enabled: false
```

**No default admin user is provisioned in `avira-platform`.** Business users self-register. `saas-admin` (in the `saas` realm) manages the platform, not end-user realms.

---

## 5. Idempotent Initialization Algorithm

The initialization algorithm MUST be idempotent: safe to run multiple times without duplicating or overwriting existing data.

### 5.1 Initialization Entry Points

| Entry Point | Trigger | Executor | Scope |
|---|---|---|---|
| `deploy/keycloak/realm-templates/*.json` import | k3s/docker-compose startup | Keycloak `--import-realm` flag (Docker entrypoint) | Full realm bootstrap (realms, clients, roles, anonymous users) |
| `iam-service` startup `SharedRealmBootstrap` | Spring `@EventListener(ApplicationReadyEvent)` | iam-service `initservice` module | Verify shared realm is present; provision if missing (fallback for script-less environments) |
| `POST /api/iam/internal/init/realms` | Manual or pipeline trigger | iam-service controller | On-demand re-init of shared realm state verification |

### 5.2 Algorithm (per realm, per init entry point)

```
FOR EACH required_realm IN [saas, avira-platform]:

  1. GET /admin/realms/{realm}
     → 200: realm exists → proceed to step 2
     → 404: realm missing → POST /admin/realms with realm JSON body → verify 201

  2. FOR EACH required_client IN realm.clients:
     GET /admin/realms/{realm}/clients?clientId={clientId}
     → non-empty result: client exists → SKIP (do not overwrite secret)
     → empty result: POST /admin/realms/{realm}/clients
       IF confidential: GET client secret, store in Secrets manager / env (first-init only)

  3. FOR EACH required_user IN realm.users:
     GET /admin/realms/{realm}/users?username={username}
     → non-empty result: user exists → SKIP (do not reset credentials)
     → empty result:
       POST /admin/realms/{realm}/users (create user)
       PUT /admin/realms/{realm}/users/{userId}/reset-password (set initial credential)
       POST /admin/realms/{realm}/users/{userId}/role-mappings/realm (assign realm roles)

  4. VERIFY all required roles exist in realm:
     GET /admin/realms/{realm}/roles
     Missing roles → POST /admin/realms/{realm}/roles

  5. SET init status = READY for this realm
```

### 5.3 Critical Idempotency Rules

- **NEVER overwrite an existing client secret.** Check existence before create; skip on conflict.
- **NEVER reset credentials for an existing user.** Check existence before create; skip on conflict.
- **Check realm existence before creation.** A `409 Conflict` on realm creation is treated as "already exists" → continue.
- All Keycloak Admin API calls MUST be authenticated using master realm admin credentials (`KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`).

---

## 6. Readiness Strategy

### 6.1 Keycloak Availability Wait

Before any initialization call, iam-service MUST confirm Keycloak is reachable:

- Poll `GET {iam.init.keycloak-base-url}/health/ready` (Keycloak health endpoint).
- Retry with exponential backoff: initial delay 2s, max delay 30s, max attempts 10.
- If Keycloak does not become ready within timeout → iam-service startup FAILS with a clear error log.

### 6.2 Init Completion Health Signal

iam-service MUST expose a readiness indicator consumed by k3s startup probes:

- Existing endpoint: `POST /api/iam/internal/init/realms` (triggers init; returns `SharedRealmInitResponse`)
- Required internal state: initialization status tracked in memory by `SharedRealmBootstrap` (enum: `PENDING`, `INITIALIZING`, `READY`, `FAILED`)
- k3s readiness probe: poll iam-service `/actuator/health` (Spring Boot); gate service traffic until health is `UP`

### 6.3 Docker Compose Integration

In `docker-compose.yml`, iam-service service SHOULD declare `depends_on: keycloak: condition: service_healthy`. Keycloak's healthcheck:
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
  interval: 10s
  timeout: 5s
  retries: 10
  start_period: 30s
```

---

## 7. Trust Boundaries & Ownership

| Boundary | Owner | Components | Constraint |
|---|---|---|---|
| Keycloak bootstrap (realm JSON import) | DevOps | `deploy/keycloak/realm-templates/saas-realm.json`, `deploy/keycloak/realm-templates/avira-platform-realm.json` | Runs before any service starts; immutable after first run |
| Keycloak admin credentials | DevOps | `deploy/k3s/base/iam-service-secret.yaml`, docker-compose env | `KEYCLOAK_ADMIN` and `KEYCLOAK_ADMIN_PASSWORD` are for master realm bootstrap ONLY; iam-service uses `saas-backend` service account for all programmatic calls |
| Shared realm verification & tenant client provisioning | iam-service `initservice` module | `SharedRealmBootstrap`, `KeycloakRealmProvisioningService` impl | Called at iam-service startup; no other service may trigger Keycloak provisioning |
| Per-tenant client provisioning | iam-service `initservice` module | `KeycloakRealmProvisioningService.provisionTenantClient()` | Triggered by platform-service via `POST /internal/iam/tenants/{tenantId}/provision` |
| Token validation (JWKS only) | application-service | Spring Security resource server config | MUST NOT call any Keycloak Admin API; JWKS URL only |
| Client secrets (runtime) | iam-service config | k8s Secret / compose env | `KEYCLOAK_SAAS_CLIENT_SECRET`, `KEYCLOAK_PLATFORM_CLIENT_SECRET` — never committed to repository |

**Anti-patterns (explicitly prohibited):**
- ❌ `application-service` calling Keycloak Admin API
- ❌ `platform-service` calling Keycloak Admin API directly
- ❌ Hardcoded realm names in service code (use `RealmResolver` or config properties)
- ❌ Resetting user passwords or overwriting client secrets on repeated initialization runs
- ❌ Creating realm resources in the master realm (master realm is for bootstrap admin only)

---

## 8. Configuration Keys & Environment Variables

### 8.1 Environment Variables (Secrets — never commit)

| Variable | Required for | Default (dev only) | Notes |
|---|---|---|---|
| `KEYCLOAK_ADMIN` | Keycloak container bootstrap | `admin` | Master realm admin; used only at first-boot |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak container bootstrap | `admin` | Change in staging/prod |
| `KEYCLOAK_SAAS_CLIENT_SECRET` | iam-service → `saas-backend` | `change-me-saas-secret` | Secret for `saas-backend` confidential client |
| `KEYCLOAK_SAAS_ADMIN_PASSWORD` | Init script / realm template | `SaasAdmin@Init1` | Initial `saas-admin` user password; `temporaryPassword: true` |
| `KEYCLOAK_SAAS_ADMIN_EMAIL` | Init script / realm template | `admin@platform.local` | Initial `saas-admin` user email |
| `KEYCLOAK_PLATFORM_CLIENT_SECRET` | iam-service → `avira-platform-backend` | `change-me-backend-secret` | Secret for `avira-platform-backend` confidential client |

### 8.2 Spring Boot Config Properties (iam-service)

| Property | Config Key | Default | Notes |
|---|---|---|---|
| Shared realm name | `iam.realm.shared-name` | `avira-platform` | Used by `IamRealmProperties.sharedName()` |
| Dedicated realm prefix | `iam.realm.dedicated-prefix` | `tenant_` | Used by `IamRealmProperties.dedicatedPrefix()` |
| Keycloak base URL (init/admin) | `iam.init.keycloak-base-url` | `http://localhost:8080` | Base URL for Keycloak Admin REST API calls |
| Keycloak base URL (auth) | `iam.auth.keycloak-base-url` | `http://localhost:8080` | Base URL for OIDC/token endpoints |
| saas realm name | `iam.init.saas-realm-name` | `saas` | Name of the platform admin realm |
| Admin realm for bootstrap | `iam.init.admin-realm` | `master` | Realm used to obtain admin tokens for provisioning |
| saas backend client ID | `iam.init.saas-client-id` | `saas-backend` | Client used by iam-service for saas realm operations |

### 8.3 ConfigMap Keys (k3s — `configmap-app-env.yaml`)

Already present:
- `IAM_REALM_SHARED_NAME`
- `IAM_REALM_DEDICATED_PREFIX`
- `IAM_INIT_KEYCLOAK_BASE_URL`
- `IAM_AUTH_KEYCLOAK_BASE_URL`

To be added:
- `IAM_INIT_SAAS_REALM_NAME` → `saas`
- `IAM_INIT_ADMIN_REALM` → `master`
- `IAM_INIT_SAAS_CLIENT_ID` → `saas-backend`

### 8.4 Secret Keys (k3s — `iam-service-secret.yaml`)

Already present:
- `KEYCLOAK_ADMIN`
- `KEYCLOAK_ADMIN_PASSWORD`
- `KEYCLOAK_BACKEND_CLIENT_SECRET` → rename to `KEYCLOAK_PLATFORM_CLIENT_SECRET` for clarity

To be added:
- `KEYCLOAK_SAAS_CLIENT_SECRET`
- `KEYCLOAK_SAAS_ADMIN_PASSWORD`
- `KEYCLOAK_SAAS_ADMIN_EMAIL`

---

## 9. RealmResolver Interface Contract

Interface:

```java
public interface RealmResolver {
    String resolveRealm(String tenantId);
}
```

Responsibilities:

- Input: tenant identifier as string.
- Output: Keycloak realm name that must be used for auth/admin operations.
- Rule: callers must not hardcode realm names; always resolve through this interface.
- Note: `saas` realm is NOT routed through `RealmResolver`. It is accessed directly by initservice using `iam.init.saas-realm-name` config property only.

## 10. Behavior by Identity Mode

### SHARED_REALM (MVP — only active mode)

- Return shared realm name (default: `avira-platform`).
- Tenant isolation is enforced through `tenant_id` JWT claim, ownership checks, and tenant-scoped DB queries.
- This is the **only mode that may be activated in MVP**.

### DEDICATED_REALM (post-MVP — scaffolded only)

- Return tenant-specific realm name using configured prefix.
- Default naming convention: `tenant_<tenantId>`.
- Supports full identity isolation per tenant.

---

## 11. Current Implementation Notes

Current implementation in iam-service:

- `RealmResolver` → `SharedOrDedicatedRealmResolver` (enforces SHARED_REALM for MVP)
- `KeycloakRealmProvisioningService` → `InMemoryKeycloakRealmProvisioningService` (placeholder; **must be replaced** with `RestKeycloakRealmProvisioningService` before production)
- `IamRealmProperties` (record): `sharedName`, `dedicatedPrefix` — new fields needed: `saasRealmName`, `adminRealm`, `saasClientId`

Resolution behavior (`SharedOrDedicatedRealmResolver`):

1. Parse `tenantId` as UUID.
2. If UUID parse fails, fallback to `tenant_<tenantId>` naming.
3. If parse succeeds, load `TenantRealmConfig` by tenant id.
4. If identity mode is `SHARED_REALM`, return configured shared realm.
5. Otherwise return dedicated realm using configured prefix.

---

## 12. Usage Notes

- Use `RealmResolver` in all tenant-scoped services/components that need realm selection.
- Use `iam.init.saas-realm-name` config property directly in `initservice` for platform admin realm operations.
- Do not use realm name as tenant identifier.
- Do not create realm provisioning logic outside iam-service.
- In `application-service`, validate tokens only; never call Keycloak Admin API.
- Validate tenant ownership before any realm-dependent operation.
- Initialization scripts (`deploy/keycloak/realm-templates/`) are the primary bootstrap mechanism; iam-service initservice is the fallback/verification layer.

## Error Handling Guidance

- If tenant config is missing for UUID input, fail fast with a clear error.
- Log tenant id and selected mode/realm at debug level for diagnostics.
- Keep resolver deterministic and side-effect free.
