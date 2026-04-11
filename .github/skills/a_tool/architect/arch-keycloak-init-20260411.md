# Architecture Artifact: Keycloak Initialization Contract

**Source Plan:** `.github/skills/a_tool/plan/po-plan-20260411-keycloak-init.md`  
**Date:** 2026-04-11  
**Status:** Implementation-ready  
**Full Contract:** `docs/iam-service-base/realm-resolution.md`

---

## 1. Scope Summary

Freeze the API/data contract and service boundaries for Keycloak initialization so that DevOps scripts and iam-service backend work can proceed in parallel.

In scope:
- Two pre-provisioned realms: `saas` (platform admin) and `avira-platform` (shared tenant)
- Required confidential + public clients per realm with exact clientIds
- Required users (saas-admin, anonymous-saas, anonymous-platform) with credential strategy
- Idempotent initialization algorithm and readiness strategy
- Trust boundary map: which module/scripts own each operation
- All config keys and env vars

Out of scope (MVP):
- DEDICATED_REALM provisioning
- Production secrets management (Vault, external KMS)
- Keycloak theming or custom authentication flows
- External IdP federation

---

## 2. Service Boundary Map

```
┌──────────────────────────────────────────────────────────────────┐
│  DevOps: deploy/keycloak/realm-templates/                        │
│  saas-realm.json + avira-platform-realm.json                     │
│  → Imported via Keycloak --import-realm at container startup     │
│  → Primary bootstrap: realms, clients, roles, anonymous users    │
└───────────────────────────────┬──────────────────────────────────┘
                                │ One-time import (idempotent on restart
                                │ only if --import-realm strategy allows)
                                ▼
┌──────────────────────────────────────────────────────────────────┐
│  Keycloak :8080                                                  │
│  Realms: master (bootstrap), saas, avira-platform                │
└───────────────────────────┬──────────────────────────────────────┘
                            │ Admin REST API (iam-service ONLY)
                            │ Auth: saas-backend (client_credentials)
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│  iam-service :8081                                               │
│  initservice: SharedRealmBootstrap                               │
│  → Startup verify: polls Keycloak ready, confirms realms/clients │
│  → Fallback: calls KeycloakRealmProvisioningService if missing   │
│  → Tenant provisioning: provisionTenantClient() on demand        │
└───────────────────────────┬──────────────────────────────────────┘
                            │ POST /internal/iam/tenants/{tenantId}/provision
                            │ (called by platform-service after tenant creation)
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│  platform-service :8082                                          │
│  → Tenant creation triggers iam-service provisioning call        │
│  → NEVER calls Keycloak Admin API directly                        │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│  application-service :8083                                       │
│  → Token validation via Keycloak JWKS endpoint ONLY             │
│  → MUST NOT call any Keycloak Admin API                          │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. Realm Model

| Realm | Name | Owner | Bootstrap Method |
|---|---|---|---|
| Platform Admin | `saas` | DevOps (JSON template) | `deploy/keycloak/realm-templates/saas-realm.json` |
| Shared Tenant | `avira-platform` | DevOps (JSON template) + iam-service fallback | `deploy/keycloak/realm-templates/avira-platform-realm.json` |
| Dedicated Tenant | `tenant_{tenantId}` | iam-service (post-MVP only) | `KeycloakRealmProvisioningService` (not activated in MVP) |

---

## 4. Required Clients (Frozen Contract)

### `saas` realm

| clientId | Type | Grant Type | iam-service binding |
|---|---|---|---|
| `saas-backend` | Confidential | `client_credentials` | `iam.init.saas-client-id` / `KEYCLOAK_SAAS_CLIENT_SECRET` |
| `saas-console` | Public | `authorization_code` + PKCE | Browser login only; no backend binding |

### `avira-platform` realm

| clientId | Type | Grant Type | iam-service binding |
|---|---|---|---|
| `avira-platform-backend` | Confidential | `client_credentials` + `password` | `KEYCLOAK_PLATFORM_CLIENT_SECRET` (was: `KEYCLOAK_BACKEND_CLIENT_SECRET`) |
| `avira-platform-public` | Public | `authorization_code` + PKCE | saas-ui-app Angular app |

### Per-tenant (dynamic, `avira-platform` realm)

| clientId pattern | Type | Provisioner |
|---|---|---|
| `tenant-{tenantId}-backend` | Confidential | `KeycloakRealmProvisioningService.provisionTenantClient()` |

---

## 5. Required Users (Frozen Contract)

| Realm | Username | Roles | Attributes | Credential Strategy |
|---|---|---|---|---|
| `saas` | `saas-admin` | `platform-admin` | `platform_role: PLATFORM_ADMIN`, `email: $KEYCLOAK_SAAS_ADMIN_EMAIL` | Initial password: `$KEYCLOAK_SAAS_ADMIN_PASSWORD`; `temporaryPassword: true` |
| `saas` | `anonymous-saas` | `anonymous` | `platform_role: ANONYMOUS`, `enabled: false` | Random UUID; `enabled: false` (no direct login) |
| `avira-platform` | `anonymous-platform` | `anonymous` | `platform_role: ANONYMOUS`, `enabled: false` | Random UUID; `enabled: false` (no direct login) |

**No default admin user in `avira-platform`.** Business users self-register via `POST /api/iam/auth/register` (future endpoint).

---

## 6. Idempotent Initialization Algorithm

Full algorithm in `docs/iam-service-base/realm-resolution.md` Section 5. Summary:

1. For each required realm: check existence → create if missing.
2. For each required client: check existence → create if missing, **skip if present** (never overwrite secret).
3. For each required user: check existence → create if missing, **skip if present** (never reset credentials).
4. For each required role: check existence → create if missing.

**Idempotency invariants:**
- `409 Conflict` on create = already exists = proceed.
- Empty `GET` results = not found = create.
- Non-empty `GET` results = exists = skip mutation.

---

## 7. Readiness Strategy

| Layer | Mechanism |
|---|---|
| Keycloak container | `healthcheck` on `/health/ready`; dependent services wait |
| iam-service startup | `SharedRealmBootstrap` polls Keycloak with retry/backoff before calling Admin API |
| k3s probe | `/actuator/health` on iam-service; gates ingress traffic |
| docker-compose | `depends_on: keycloak: condition: service_healthy` + iam-service `condition: service_healthy` |

---

## 8. Trust Boundaries

| Caller | Target | Allowed | Auth |
|---|---|---|---|
| DevOps init script | Keycloak Admin REST | ✅ JSON realm import | `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` (master realm) |
| iam-service initservice | Keycloak Admin REST | ✅ Realm verify + tenant client provision | `saas-backend` client credentials |
| platform-service | iam-service `/internal/iam/tenants/{id}/provision` | ✅ Internal REST | Service account token (internal network) |
| application-service | Keycloak Admin REST | ❌ PROHIBITED | N/A |
| platform-service | Keycloak Admin REST | ❌ PROHIBITED | N/A |
| Any service | Realm hardcoded strings in code | ❌ PROHIBITED | N/A — use `RealmResolver` |

---

## 9. API Contract (Existing — no new endpoints needed)

All existing endpoints in `RealmProvisioningController` are sufficient for this phase:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/iam/internal/init/realms` | Trigger shared realm init / verify |
| `POST` | `/api/iam/internal/init/tenants/{tenantId}` | Provision per-tenant client |
| `POST` | `/api/iam/client/tenant-realm-configs` | Upsert tenant realm config (SHARED_REALM enforced) |
| `GET` | `/api/iam/client/realms/tenants/{tenantId}` | Resolve realm for a tenantId |

---

## 10. Parallel Work Packages

### Backend (iam-service) — can run in parallel with DevOps

| Task | Module | File | Priority |
|---|---|---|---|
| Implement `RestKeycloakRealmProvisioningService` (replaces in-memory) | initservice | `realm/service/RestKeycloakRealmProvisioningService.java` | High — MVP blocker |
| Extend `IamRealmProperties` with `saasRealmName`, `adminRealm`, `saasClientId` | initservice config | `realm/config/IamRealmProperties.java` | High |
| Implement `SharedRealmBootstrap` (startup verify + fallback init) | initservice | `realm/service/SharedRealmBootstrap.java` | High |
| Add Keycloak readiness poll with retry/backoff | initservice | `realm/service/KeycloakReadinessWatcher.java` | High |
| Wire `directAccessGrantsEnabled` and token exchange in auth flow | authservice | `auth/service/AuthService.java` | Medium |

**No new REST endpoints required for this phase.**

### DevOps — can run in parallel with Backend

| Task | File | Priority |
|---|---|---|
| Author `saas-realm.json` realm template | `deploy/keycloak/realm-templates/saas-realm.json` | High — MVP blocker |
| Author `avira-platform-realm.json` realm template | `deploy/keycloak/realm-templates/avira-platform-realm.json` | High — MVP blocker |
| Update `docker-compose.yml`: Keycloak healthcheck + `--import-realm` | `docker-compose.yml` | High |
| Update `deploy/k3s/base/iam-service-secret.yaml`: add new secret keys | `deploy/k3s/base/iam-service-secret.yaml` | High |
| Update `deploy/k3s/base/configmap-app-env.yaml`: add new config keys | `deploy/k3s/base/configmap-app-env.yaml` | High |
| Add Keycloak deployment + service to k3s base | `deploy/k3s/base/keycloak-deployment.yaml`, `keycloak-service.yaml` | High |
| Author init script (fallback) | `scripts/init-keycloak.sh` | Medium |

---

## 11. Assumptions & Open Questions

| # | Assumption / Open Question | Impact |
|---|---|---|
| A1 | Keycloak JSON realm import with `--import-realm` is the primary bootstrap path (not kcadm.sh) | Determines Docker entrypoint command for Keycloak in compose/k3s |
| A2 | `saas-admin` temporary password is acceptable for MVP local dev; must be documented in README | Low risk for dev; high risk for staging without rotation |
| A3 | `avira-platform-backend` uses `directAccessGrantsEnabled: true` for MVP token exchange simplicity | Must be disabled post-MVP in favour of PKCE-only flows |
| A4 | `KEYCLOAK_BACKEND_CLIENT_SECRET` (existing secret key) maps to `avira-platform-backend` client | Rename to `KEYCLOAK_PLATFORM_CLIENT_SECRET` to avoid ambiguity; update all references |
| OQ1 | Does saas-ui-app need to authenticate against `saas` realm (for platform admin) and `avira-platform` (for business users) separately, or is there a single realm for both? | If platform admin UI shares the Angular app, it needs to support realm selection at login |
| OQ2 | Should `saas-backend` client (used by iam-service) live in `saas` realm or `master` realm? | Placing it in `saas` realm is cleaner (no dependency on master realm for routine ops) — assumed above |

---

## 12. Artifact Paths

| Artifact | Path | Status |
|---|---|---|
| Full contract (authoritative) | `docs/iam-service-base/realm-resolution.md` | Updated (2026-04-11) |
| This architect artifact | `.github/skills/a_tool/architect/arch-keycloak-init-20260411.md` | Created (2026-04-11) |
| saas realm template | `deploy/keycloak/realm-templates/saas-realm.json` | Pending DevOps |
| avira-platform realm template | `deploy/keycloak/realm-templates/avira-platform-realm.json` | Pending DevOps |
| Init script | `scripts/init-keycloak.sh` | Pending DevOps |
| Keycloak k3s deployment | `deploy/k3s/base/keycloak-deployment.yaml` | Pending DevOps |
| Backend init implementation | `iam-service/.../realm/service/RestKeycloakRealmProvisioningService.java` | Pending Backend |
