# Realm Resolution

This document defines the RealmResolver contract and expected behavior for shared and dedicated tenant identity modes.

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

### SHARED_REALM

- Return shared realm name (default: `avira-platform`).
- Tenant isolation is still required through tenant claims, ownership checks, and tenant-scoped queries.

### DEDICATED_REALM

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
