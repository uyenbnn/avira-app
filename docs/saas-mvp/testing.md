# SaaS MVP Integration Testing Report

Date: 2026-04-11
Scope source:
- .github/skills/a_tool/plan/po-plan-20260411-saas-mvp.md
- .github/skills/a_tool/architect/flow-saas-mvp.md

## Tests Changed

- integration-tests/node-axios/tests/_mvp-common.js
- integration-tests/node-axios/tests/mvp-tenant-creation-shared-realm.integration.js
- integration-tests/node-axios/tests/mvp-application-creation-under-tenant.integration.js
- integration-tests/node-axios/tests/mvp-iam-shared-realm-init-provision.integration.js
- integration-tests/node-axios/tests/mvp-app-token-exchange-shape.integration.js
- integration-tests/node-axios/src/config.js
- integration-tests/node-axios/src/httpClient.js
- integration-tests/node-axios/package.json
- integration-tests/node-axios/.env.example

## Use-Case to File Mapping

- Tenant creation (SHARED_REALM): integration-tests/node-axios/tests/mvp-tenant-creation-shared-realm.integration.js
- Application creation under tenant: integration-tests/node-axios/tests/mvp-application-creation-under-tenant.integration.js
- IAM shared realm init/provision endpoints: integration-tests/node-axios/tests/mvp-iam-shared-realm-init-provision.integration.js
- App token exchange endpoint shape: integration-tests/node-axios/tests/mvp-app-token-exchange-shape.integration.js

## Execution Commands

Executed from: D:/project/avira-app/integration-tests/node-axios

1. npm run test:mvp:docker
2. node tests/mvp-tenant-creation-shared-realm.integration.js --env .env.docker
3. node tests/mvp-application-creation-under-tenant.integration.js --env .env.docker
4. node tests/mvp-iam-shared-realm-init-provision.integration.js --env .env.docker
5. node tests/mvp-app-token-exchange-shape.integration.js --env .env.docker

## Execution Results

All executed tests failed fast due environment/network unavailability (ECONNREFUSED).

- mvp-tenant-creation-shared-realm: FAIL
  - Error: HTTP post /api/platform/tenants failed: message=unknown network error code=ECONNREFUSED baseURL=http://localhost:10004
- mvp-application-creation-under-tenant: FAIL
  - Error: HTTP post /api/platform/tenants failed: message=unknown network error code=ECONNREFUSED baseURL=http://localhost:10004
- mvp-iam-shared-realm-init-provision: FAIL
  - Error: HTTP post /api/iam/internal/init/realms failed: message=unknown network error code=ECONNREFUSED baseURL=http://localhost:8081
- mvp-app-token-exchange-shape: FAIL
  - Error: HTTP post /api/apps/mvp-app-shape/auth/token-exchange failed: message=unknown network error code=ECONNREFUSED baseURL=http://localhost:10001

## Failure Reports (Reproducible)

### Failure 1: Tenant create endpoint unreachable
- Step: Execute `node tests/mvp-tenant-creation-shared-realm.integration.js --env .env.docker`
- Expected: `201 Created` from `POST /api/platform/tenants` with `identityMode=SHARED_REALM`
- Actual: `ECONNREFUSED` on base URL `http://localhost:10004`
- Suspected layer: Platform service runtime/network binding (service down or not listening on configured port)
- Ownership: Platform Service team + Local environment/deployment owner

### Failure 2: Application create workflow blocked by tenant endpoint unavailability
- Step: Execute `node tests/mvp-application-creation-under-tenant.integration.js --env .env.docker`
- Expected: Tenant create `201`, app create `201`, app list `200`
- Actual: First call `POST /api/platform/tenants` fails with `ECONNREFUSED` on `http://localhost:10004`
- Suspected layer: Platform service runtime/network binding
- Ownership: Platform Service team + Local environment/deployment owner

### Failure 3: IAM init/provision endpoints unreachable
- Step: Execute `node tests/mvp-iam-shared-realm-init-provision.integration.js --env .env.docker`
- Expected: `POST /api/iam/internal/init/realms` returns `200`, provisioning call returns `200`
- Actual: `ECONNREFUSED` on base URL `http://localhost:8081`
- Suspected layer: IAM service runtime/network binding (service down or not listening on configured port)
- Ownership: IAM Service team + Local environment/deployment owner

### Failure 4: App token exchange endpoint unreachable
- Step: Execute `node tests/mvp-app-token-exchange-shape.integration.js --env .env.docker`
- Expected: `200` from `POST /api/apps/{appId}/auth/token-exchange` with contract fields present
- Actual: `ECONNREFUSED` on base URL `http://localhost:10001`
- Suspected layer: API gateway/application-service routing runtime
- Ownership: API Gateway/Kong owner + Application Service team + Local environment/deployment owner

## Ownership Routing Summary

- Platform API failures: Platform Service owner, then local deployment/runtime owner.
- IAM API failures: IAM Service owner, then local deployment/runtime owner.
- App token exchange route failures: Kong/Gateway owner and Application Service owner, then local deployment/runtime owner.

## Deployment Readiness

Status: NOT READY

Reason: Core MVP workflow endpoints are not reachable in the current local environment (connection refused on platform, IAM, and app/gateway targets), so integration validation cannot complete.

## Notes

- Tests are implemented with one use case per file under integration-tests/node-axios/tests.
- New scripts added to package.json for `.env.docker`, `.env.kong`, `.env.k3s`:
  - `test:mvp:docker`
  - `test:mvp:kong`
  - `test:mvp:k3s`

## Local k3s redeploy and reachability (2026-04-11)

Use repository root:

```powershell
cd D:\project\avira-app
```

Deploy current manifests/images:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/deploy-k3s-local.ps1
```

Expose endpoints from host (separate terminals):

```powershell
kubectl -n avira port-forward svc/kong 10001:8000
kubectl -n avira port-forward svc/iam-service 8081:8080
kubectl -n avira port-forward svc/platform-service 10004:8080
kubectl -n avira port-forward svc/application-service 10002:8080
```

Expected test targets:

- Gateway base: `http://localhost:10001`
- IAM direct base: `http://localhost:8081`
- Platform direct base: `http://localhost:10004`
- Application direct base: `http://localhost:10002`

Run MVP integration checks:

```powershell
cd D:\project\avira-app\integration-tests\node-axios
npm run test:mvp:docker
```

Caveat:

- IAM Keycloak URL in k3s is configured as `http://host.k3d.internal:8080`. If your local k3s does not resolve this hostname, switch to a resolvable host alias in `deploy/k3s/base/configmap-app-env.yaml`.
