# Architecture Artifact: SaaS MVP Flow

**Source:** `.github/skills/a_tool/plan/po-plan-20260411-saas-mvp.md`  
**Date:** 2026-04-11  
**Status:** Implementation-ready

---

## 1. Scope Summary

MVP delivers three user roles running locally via k3s:

| Role | Actions |
|---|---|
| Platform Admin | View all tenants/apps, assign permissions |
| Business User | Register, login, auto-create tenant, CRUD apps, configure app (domain, auth mode) |
| End User | Authenticate and access a registered application |

Out of scope for MVP: DEDICATED_REALM mode, billing, external IdP, production hardening.

---

## 2. Service Boundary Map

```
┌─────────────────────────────────────────────────────────────┐
│  Browser / Angular (saas-ui-app)                            │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTPS (JWT Bearer — except /auth/*)
                        ▼
┌─────────────────────────────────────────────────────────────┐
│  Kong API Gateway                                           │
│  Routes: /api/iam/*, /api/platform/*, /api/apps/*           │
│  Enforces: JWT verification, rate limiting                  │
└──────────┬──────────────────┬──────────────────┬────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
┌──────────────────┐ ┌──────────────────┐ ┌─────────────────────┐
│  iam-service     │ │  platform-service│ │  application-service│
│  :8081           │ │  :8082           │ │  :8083              │
│                  │ │                  │ │                     │
│ - Auth (login,   │ │ - Tenant CRUD    │ │ - App user auth     │
│   refresh,       │ │ - App lifecycle  │ │ - App-scoped JWT    │
│   logout)        │ │ - Config store   │ │ - App user CRUD     │
│ - Realm          │ │                  │ │                     │
│   provisioning   │ │                  │ │                     │
│ - Platform users │ │                  │ │                     │
│ - Keycloak Admin │ │                  │ │                     │
└──────────┬───────┘ └────────┬─────────┘ └──────────┬──────────┘
           │                  │ internal REST         │
           │◄─────────────────┘  (tenant provisioned  │
           │                   on tenant creation)     │
           │                                           │
           ▼                                           │
    ┌─────────────┐                            ┌───────▼────────┐
    │  Keycloak   │◄── Admin API (iam only) ───┤  (token valid. │
    │  :8080      │                            │   via JWKS     │
    └─────────────┘                            │   endpoint)    │
                                               └────────────────┘
           │
           ▼
    ┌───────────────────────────────────┐
    │  PostgreSQL                       │
    │  DB: iam_db, platform_db, app_db  │
    └───────────────────────────────────┘
```

### Inter-service Communication (MVP)

Synchronous internal REST over cluster network (post-MVP: async events via RabbitMQ Stream).

| Caller | Target | Trigger | Endpoint |
|---|---|---|---|
| platform-service | iam-service | Tenant created | `POST /internal/iam/tenants/{tenantId}/provision` |
| application-service | iam-service (JWKS) | Token validation | Keycloak JWKS URL (no Admin API) |

---

## 3. Trust Boundaries

| Zone | Who | Auth requirement |
|---|---|---|
| Public | Any browser client | None — login/register endpoints |
| Authenticated | Logged-in user | JWT Bearer (Keycloak-issued, realm: `avira-platform`) |
| Internal | Service-to-service | Internal network + service account token (not user JWT) |
| Privileged | iam-service → Keycloak | Keycloak Admin client credentials (service account) |

**Non-negotiable:**
- `tenant_id` and `app_id` are ALWAYS derived from the validated JWT claim, never from client-provided body/path params.
- platform-service validates token ownership before mutating any tenant/app resource.
- application-service MUST NOT call Keycloak Admin API under any circumstances.

---

## 4. Tenant Isolation Constraints

| Constraint | Rule |
|---|---|
| Data isolation | All DB queries include `WHERE tenant_id = ?` derived from JWT |
| Identity isolation | MVP: SHARED_REALM only (`avira-platform`). DEDICATED_REALM is out of scope |
| App isolation | All app-scoped queries include `WHERE app_id = ?` and `tenant_id = ?` |
| Realm routing | All Keycloak operations pass through `RealmResolver.resolveRealm(tenantId)` |
| Cross-tenant access | Blocked at service layer; never inferred from request path |

---

## 5. API Contracts

### 5.1 platform-service (`com.avira.platform`)

Base path: `/api/platform`  
Security: JWT Bearer required (except where noted). `tenant_id` extracted from JWT.

#### Tenant Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/tenants` | Business User | Auto-create tenant on first registration |
| GET | `/tenants/{tenantId}` | Business User, Platform Admin | Get tenant details |
| PATCH | `/tenants/{tenantId}` | Business User (owner), Platform Admin | Update tenant settings |
| GET | `/tenants` | Platform Admin | List all tenants |

**`TenantRequest` (POST body / PATCH body)**
```json
{
  "name": "string",          // required on create
  "contactEmail": "string",  // required on create
  "status": "ACTIVE|SUSPENDED" // PATCH only, admin only
}
```

**`TenantResponse`**
```json
{
  "tenantId": "uuid",
  "name": "string",
  "contactEmail": "string",
  "identityMode": "SHARED_REALM",
  "status": "ACTIVE|SUSPENDED|PENDING",
  "createdAt": "ISO-8601"
}
```

#### Application Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/tenants/{tenantId}/applications` | Business User (tenant owner) | Create application |
| GET | `/tenants/{tenantId}/applications` | Business User (tenant owner), Platform Admin | List applications |
| GET | `/tenants/{tenantId}/applications/{appId}` | Business User (tenant owner), Platform Admin | Get application |
| PATCH | `/tenants/{tenantId}/applications/{appId}` | Business User (tenant owner) | Update app config |
| DELETE | `/tenants/{tenantId}/applications/{appId}` | Business User (tenant owner), Platform Admin | Delete app |

**`ApplicationRequest` (POST body)**
```json
{
  "name": "string",         // required
  "domain": "string",       // required, e.g. "my-app.example.com"
  "authMode": "KEYCLOAK|CUSTOM_JWT|PASSTHROUGH", // required
  "config": { }             // optional, key-value app config
}
```

**`ApplicationPatchRequest` (PATCH body — all optional)**
```json
{
  "name": "string",
  "domain": "string",
  "authMode": "KEYCLOAK|CUSTOM_JWT|PASSTHROUGH",
  "config": { },
  "status": "ACTIVE|INACTIVE"
}
```

**`ApplicationResponse`**
```json
{
  "appId": "uuid",
  "tenantId": "uuid",
  "name": "string",
  "domain": "string",
  "authMode": "KEYCLOAK|CUSTOM_JWT|PASSTHROUGH",
  "status": "ACTIVE|INACTIVE|PROVISIONING",
  "config": { },
  "createdAt": "ISO-8601"
}
```

#### Status Codes

| Code | Meaning |
|---|---|
| 200 | OK (GET, PATCH) |
| 201 | Created (POST) |
| 204 | Deleted (DELETE) |
| 400 | Validation failure |
| 403 | Caller is not tenant owner or does not have required role |
| 404 | Tenant or application not found |
| 409 | Duplicate name within tenant |

#### Internal Endpoint (not exposed via Kong)

| Method | Path | Caller | Description |
|---|---|---|---|
| POST | `/internal/notify/tenant-created` | iam-service callback (future) | Reserved; not used in MVP sync flow |

---

### 5.2 iam-service (`com.avira.iam`)

Base path: `/api/iam`  
Keycloak Admin API: used by `iam-service` only, via `KeycloakRealmProvisioningService` interface.

#### Auth Endpoints (public)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | None | Exchange credentials for token pair |
| POST | `/auth/refresh` | Refresh token in body | Refresh access token |
| POST | `/auth/logout` | Bearer token | Invalidate session |

**`LoginRequest`**
```json
{
  "email": "string",
  "password": "string"
}
```

**`TokenResponse`**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 300,
  "tokenType": "Bearer"
}
```

**`RefreshRequest`**
```json
{
  "refreshToken": "string"
}
```

#### Provisioning Endpoints (internal — not via Kong)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/internal/init/realms` | Service account | Bootstrap shared realm `avira-platform` (idempotent) |
| POST | `/internal/init/tenants/{tenantId}` | Service account | Provision Keycloak client for new tenant in shared realm |

**`TenantProvisionRequest` (POST `/internal/init/tenants/{tenantId}`)**
```json
{
  "tenantId": "uuid",
  "tenantName": "string",
  "contactEmail": "string",
  "identityMode": "SHARED_REALM"
}
```

**`TenantProvisionResponse`**
```json
{
  "tenantId": "uuid",
  "realm": "avira-platform",
  "keycloakClientId": "string",
  "status": "PROVISIONED|ALREADY_EXISTS"
}
```

#### Realm Config Endpoints (admin only)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/client/tenant-realm-configs` | Platform Admin | Upsert realm config for tenant |
| GET | `/client/realms/tenants/{tenantId}` | Authenticated | Resolve realm name for tenant |

**`TenantRealmConfigRequest`**
```json
{
  "tenantId": "uuid",
  "identityMode": "SHARED_REALM",
  "dedicatedRealmApproved": false
}
```

**`RealmResolutionResponse`**
```json
{
  "tenantId": "uuid",
  "realm": "avira-platform",
  "identityMode": "SHARED_REALM"
}
```

#### Platform User Endpoints

| Method | Path | Auth | Role | Description |
|---|---|---|---|---|
| POST | `/users` | None (registration) | — | Register platform user; triggers tenant auto-creation |
| GET | `/users/{userId}` | Bearer | Self or Admin | Get user |
| PATCH | `/users/{userId}` | Bearer | Self or Admin | Update user |
| GET | `/users` | Bearer | Platform Admin | List all users |

**`UserRegistrationRequest`**
```json
{
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "tenantName": "string"
}
```

**`UserResponse`**
```json
{
  "userId": "uuid",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "tenantId": "uuid",
  "roles": ["BUSINESS_USER"],
  "createdAt": "ISO-8601"
}
```

#### Role Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/roles/users/{userId}` | Platform Admin | Assign role to user |
| GET | `/roles/users/{userId}` | Platform Admin, Self | List roles for user |

**`RoleAssignRequest`**
```json
{
  "role": "PLATFORM_ADMIN|BUSINESS_USER|END_USER"
}
```

#### Status Codes

| Code | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 400 | Validation failure |
| 401 | Missing or invalid token |
| 403 | Insufficient role |
| 404 | Not found |
| 409 | User already exists |

---

### 5.3 application-service (`com.avira.application`)

Base path: `/api/apps`  
Security: JWT Bearer required (Keycloak-issued, validated via JWKS). NO Keycloak Admin API calls.

#### App Auth Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/{appId}/auth/login` | None | App-scoped login; validates platform JWT, issues app JWT |
| POST | `/{appId}/auth/refresh` | App refresh token | Refresh app JWT |
| POST | `/{appId}/auth/logout` | App Bearer token | Invalidate app session |

**`AppLoginRequest`**
```json
{
  "email": "string",
  "password": "string"
}
```

**`AppTokenResponse`**
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 300,
  "tokenType": "Bearer",
  "appId": "uuid",
  "tenantId": "uuid"
}
```

#### App User Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/{appId}/users` | App Bearer | Register end user for app |
| GET | `/{appId}/users/{userId}` | App Bearer | Get app user |
| PATCH | `/{appId}/users/{userId}` | App Bearer | Update app user |
| GET | `/{appId}/users` | App Bearer (admin role) | List users for app |

**`AppUserRequest`**
```json
{
  "email": "string",
  "password": "string",
  "displayName": "string"
}
```

**`AppUserResponse`**
```json
{
  "userId": "uuid",
  "appId": "uuid",
  "tenantId": "uuid",
  "email": "string",
  "displayName": "string",
  "status": "ACTIVE|INACTIVE",
  "createdAt": "ISO-8601"
}
```

#### Status Codes

Same as iam-service table above.

---

## 6. Parallel Work Packages

### Backend Dev

| # | Package | Service | Artifacts | Depends on |
|---|---|---|---|---|
| BE-1 | Shared realm bootstrap | iam-service | `initservice` — `SharedRealmBootstrap`, `KeycloakRealmProvisioningService` impl | Keycloak running |
| BE-2 | Platform user registration + login | iam-service | `userservice` controller/service, `authenticationservice` controller/service | BE-1 |
| BE-3 | Tenant + App CRUD | platform-service | `tenant-service`, `application-service` modules (controller, service, repo, DTOs) | BE-2 |
| BE-4 | Tenant provisioning on create | platform-service → iam-service | Internal REST call: `POST /internal/init/tenants/{tenantId}` in iam-service; invoked from platform-service after tenant save | BE-1, BE-3 |
| BE-5 | App auth + app user CRUD | application-service | `authentication-service`, `administration-service` modules | BE-2 |
| BE-6 | Role/permission assignment | iam-service | `roleservice`, `permissionservice` | BE-2 |

Files per service:
- `com.avira.iam.<module>.controller`, `.service`, `.repository`, `.dto`, `.mapper`
- `com.avira.platform.<module>.controller`, `.service`, `.repository`, `.dto`, `.mapper`
- `com.avira.application.<module>.controller`, `.service`, `.repository`, `.dto`, `.mapper`

### Frontend Dev

| # | Package | Artifacts | Depends on |
|---|---|---|---|
| FE-1 | Auth shell (login / register) | `saas-ui-app/src/app/auth/` — login, register components + OIDC redirect guard | BE-2 |
| FE-2 | Business user dashboard | `saas-ui-app/src/app/dashboard/` — tenant info, app list | BE-3, FE-1 |
| FE-3 | App detail / setup | `saas-ui-app/src/app/apps/` — create/edit app, domain+authMode config | BE-3, FE-2 |
| FE-4 | Platform admin view | `saas-ui-app/src/app/admin/` — tenant list, user list, role assignment | BE-6, FE-1 |
| FE-5 | End user app access | `saas-ui-app/src/app/app-portal/` — app login, user CRUD | BE-5, FE-1 |

---

## 7. Assumptions and Open Questions

### Assumptions
1. MVP uses **SHARED_REALM only** (`avira-platform`). DEDICATED_REALM provisioning is scaffolded but not activated.
2. Tenant auto-creation is triggered synchronously via `POST /api/platform/tenants` immediately after user registration succeeds in iam-service.
3. platform-service calls `iam-service` internal endpoint synchronously on tenant creation (async RabbitMQ stream is post-MVP).
4. application-service validates tokens via Keycloak JWKS endpoint (no Admin API).
5. Kong API Gateway routes on path prefix matching; JWT validation plugin is enabled for all routes except `/api/iam/auth/*` and `/api/iam/users` (registration).
6. All services use separate PostgreSQL databases (`iam_db`, `platform_db`, `app_db`) within the same Postgres instance for MVP.
7. `identityMode` field defaults to `SHARED_REALM` in `TenantRealmConfig` for all MVP tenants.

### Open Questions
- Q1: Should `POST /api/iam/users` (registration) automatically trigger `POST /api/platform/tenants`, or should the UI call both endpoints sequentially? **Recommendation:** iam-service calls platform-service internally to keep the UI flow atomic.
- Q2: `authMode: CUSTOM_JWT` — is custom JWT issuance in application-service required for MVP, or is KEYCLOAK passthrough sufficient? **Recommendation:** defer CUSTOM_JWT to post-MVP; implement KEYCLOAK mode only.
- Q3: Platform Admin role — is it seeded via Keycloak realm role or via DB? **Recommendation:** seed via Flyway in `iam_db` and map to Keycloak realm role at bootstrap.
