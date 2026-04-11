# Realm Resolution

This document defines the RealmResolver contract, MVP realm strategy, and explicit constraints for shared and dedicated tenant identity modes.

---

## MVP Realm Strategy (2026-04-11)

**Active for MVP:** `SHARED_REALM` only.

| Decision | Value |
|---|---|
| Default realm | `avira-platform` |
| Identity mode at tenant creation | `SHARED_REALM` (hardcoded for MVP) |
| `DEDICATED_REALM` activation | **Out of scope for MVP.** Interface and data model are scaffolded but cannot be activated without explicit approval gate and dedicated-realm provisioning implementation. |
| Tenant isolation mechanism | `tenant_id` JWT claim + scoped DB queries. NOT by realm. |

### Non-Negotiable MVP Constraints

1. `identityMode` field on `TenantRealmConfig` MUST be set to `SHARED_REALM` for all tenants created in MVP.
2. `dedicatedRealmApproved = false` MUST be enforced at the API level; the field is not user-settable in MVP.
3. `RealmResolver.resolveRealm(tenantId)` MUST be used for every Keycloak operation — no hardcoded realm strings anywhere except the default config property.
4. Realm provisioning (create/delete Keycloak realm) is callable only through `KeycloakRealmProvisioningService` inside `iam-service`. No other service may call Keycloak Admin API.
5. `application-service` validates tokens against Keycloak JWKS endpoint only; it MUST NOT perform any Keycloak Admin API call.

---

## Interface Contract

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

## Behavior by Identity Mode

### SHARED_REALM (MVP — only active mode)

- Return shared realm name (default: `avira-platform`).
- Tenant isolation is enforced through `tenant_id` JWT claim, ownership checks, and tenant-scoped DB queries.
- This is the **only mode that may be activated in MVP**.

### DEDICATED_REALM (post-MVP — scaffolded only)

- Return tenant-specific realm name using configured prefix.
- Default naming convention: `tenant_<tenantId>`.
- Supports full identity isolation per tenant.

## Current Implementation Notes

Current implementation in iam-service:

- Class: `SharedOrDedicatedRealmResolver`
- Repository dependency: `TenantRealmConfigRepository`
- Config dependency: `IamRealmProperties`

Resolution behavior:

1. Parse `tenantId` as UUID.
2. If UUID parse fails, fallback to `tenant_<tenantId>` naming.
3. If parse succeeds, load `TenantRealmConfig` by tenant id.
4. If identity mode is `SHARED_REALM`, return configured shared realm.
5. Otherwise return dedicated realm using configured prefix.

## Usage Notes

- Use `RealmResolver` in all services/components that need realm selection.
- Do not use realm name as tenant identifier.
- Do not create realm provisioning logic outside iam-service.
- In `application-service`, validate tokens only; never call Keycloak Admin API.
- Validate tenant ownership before any realm-dependent operation.

## Error Handling Guidance

- If tenant config is missing for UUID input, fail fast with a clear error.
- Log tenant id and selected mode/realm at debug level for diagnostics.
- Keep resolver deterministic and side-effect free.
