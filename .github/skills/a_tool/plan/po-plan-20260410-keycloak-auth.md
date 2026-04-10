# PO Plan: Keycloak-based Authentication and Provisioning

## Goal
Provide a clear, minimal process plan to implement Keycloak-based authentication and provisioning for Avira. Deliverables enable: creating a default SaaS tenant, provisioning a shared realm (`avira-platform`), and authenticating users via the SaaS UI using Keycloak-issued tokens.

## Scope and Assumptions
- Scope:
  - Implement Keycloak integration and provisioning flows owned by `iam-service`.
  - Add application-side authentication support in `application-service` (validate tokens, issue app JWTs for INTERNAL auth_mode).
  - Provide local dev k3s/docker-compose artifacts and integration tests to validate end-to-end flows.
- Assumptions:
  - Keycloak is provided via `docker-compose.yml` (local dev) and the image/config is managed in repo root `docker-compose.yml`.
  - Realm creation, client creation, and role provisioning are implemented and executed only by `iam-service` init/provisioning modules.
  - `application-service` MUST NOT call the Keycloak Admin API to create realms/clients.
  - Default identity mode for SaaS tenants is SHARED_REALM (realm `avira-platform`). Dedicated realms are out of scope for MVP.
  - Postgres databases for services exist and are accessible in the local dev environment.

## Selected Phases and Rationale
1. Architecture — define high-level data flows, tenant/realm resolution contract, and strategy pattern for authentication (ensures design correctness before code). Rationale: avoid cross-service Keycloak anti-patterns.
2. Backend Dev — implement `iam-service` provisioning and `application-service` auth handlers (Internal + Anonymous). Rationale: core functionality lives here.
3. DevOps Local — provide docker-compose updates and k3s overlay to run Keycloak and services locally. Rationale: reproducible local environment for verification.
4. Frontend Dev — integrate UI login flows (Keycloak redirect or token exchange) in `saas-ui-app` to demonstrate authentication and token usage. Rationale: verify UX and end-to-end token exchange.
5. Tester Local — integration tests (axios-based) in `integration-tests/node-axios` to validate tenant creation, realm provisioning, and login flows. Rationale: automated verification and regression protection.

## Artifact Handoffs (exact paths)
- Architecture phase (inputs → outputs):
  - Inputs:
    - AGENTS.md (repo root)
    - .github/skills/po/SKILL.md (process rules)
  - Outputs (to Backend Dev & DevOps Local):
    - `docs/iam-service-base/realm-resolution.md` (new design doc)
    - `iam-service/README-provisioning.md` (detailed provisioning contract)

- Backend Dev (inputs → outputs):
  - Inputs:
    - `iam-service/` current codebase
    - `application-service/` current codebase
    - `docs/iam-service-base/realm-resolution.md`
    - `AGENTS.md`
  - Outputs:
    - `iam-service/src/main/java/com/avira/iam/init-service/**` (new/updated files implementing provisioning)
    - `iam-service/src/main/java/com/avira/iam/RealmResolver.java` (interface + implementation)
    - `iam-service/src/main/java/com/avira/iam/integration/keycloak/**` (Keycloak client wrappers)
    - `iam-service/src/main/resources/realm-templates/avira-platform.json` (realm template)
    - `iam-service/README-provisioning.md` (implementation notes)
    - `application-service/src/main/java/com/avira/application/authentication-service/**` (AuthenticationHandler strategy implementations for INTERNAL and ANONYMOUS)
    - `common-lib/src/main/java/com/avira/common/keycloak/**` (shared token validation utilities)

- DevOps Local (inputs → outputs):
  - Inputs:
    - `iam-service/README-provisioning.md`
    - `docker-compose.yml` (repo root)
    - `deploy/k3s/overlays/local/` manifests
  - Outputs:
    - `docker-compose.yml` (updated to include Keycloak service and volume/init config)
    - `deploy/k3s/overlays/local/keycloak.yaml` (k3s manifest for Keycloak)
    - `deploy/k3s/overlays/local/README-keycloak.md` (how to start Keycloak locally)

- Frontend Dev (inputs → outputs):
  - Inputs:
    - `saas-ui-app/src/` existing UI
    - `application-service` token endpoints and OpenAPI (or README notes)
  - Outputs:
    - `saas-ui-app/src/app/auth/` (login UI, Keycloak/OIDC adapter usage or token-exchange helpers)
    - `saas-ui-app/README-auth.md` (how to run UI against local Keycloak)

- Tester Local (inputs → outputs):
  - Inputs:
    - `integration-tests/node-axios/README.md`
    - `docker-compose.yml` and running local stack
    - `iam-service` provisioning endpoints
  - Outputs:
    - `integration-tests/node-axios/tests/keycloak-provisioning.test.js` (tests to create tenant, assert `avira-platform` exists, perform login flow)
    - `integration-tests/node-axios/package.json` updates (test scripts)

- Cross-cutting: policy/doc updates
  - `.github/skills/orchestrator/SKILL.md` — optional updates describing orchestration steps and handoffs (path: `.github/skills/orchestrator/SKILL.md`)

## Validation Approach & Acceptance Criteria
Validation approach: run local stack (docker-compose or k3s overlay), invoke API flows from integration tests, and exercise UI login. Tests must be runnable locally and validate both provisioning and authentication.

Acceptance criteria (all must pass):
- AC-1: Default SaaS tenant creation
  - Given a platform admin creates a tenant via `platform-service` API, then tenant record is created with `identity_mode=SHARED_REALM`.
- AC-2: Shared realm exists
  - The `avira-platform` realm is provisioned once (either on stack startup or first tenant creation) and realm JSON template file exists at `iam-service/src/main/resources/realm-templates/avira-platform.json`.
- AC-3: Provisioning performed exclusively by `iam-service`
  - No code in `application-service` calls Keycloak Admin API for realm/client creation. Automated scan/tests should assert this (or review PRs).
- AC-4: UI authentication
  - A user can authenticate via `saas-ui-app` using Keycloak login and obtain an application token (application JWT containing appId, tenantId, userId, roles). Integration test performs login and asserts JWT claims.
- AC-5: Integration tests pass
  - `integration-tests/node-axios` tests run against local stack and validate AC-1 through AC-4.

## Minimal Plan: Tasks, Estimates (relative), and Owners
Legend for estimates: S=Small, M=Medium, L=Large

Phase: Architecture
- Task A1: Define realm resolution contract and produce `docs/iam-service-base/realm-resolution.md` — Estimate: S — Owner: PO / Architect
- Task A2: Define authentication strategy interface and token-flow diagram — Estimate: S — Owner: Architect

Phase: Backend Dev
- Task B1: Implement `RealmResolver` interface and SHARED_REALM implementation — Estimate: M — Owner: backend-dev (iam-service)
- Task B2: Implement Keycloak provisioning module under `iam-service/init-service` (create realm template loader, create clients, create roles) — Estimate: L — Owner: backend-dev (iam-service)
- Task B3: Add shared token validation utilities to `common-lib` — Estimate: S — Owner: backend-dev (common-lib)
- Task B4: Implement `AuthenticationHandler` strategy classes in `application-service` (Internal + Anonymous) and token issuer for application JWT — Estimate: M — Owner: backend-dev (application-service)
- Task B5: Add unit tests for `RealmResolver` and auth handlers — Estimate: S — Owner: backend-dev (corresponding services)

Phase: DevOps Local
- Task D1: Add Keycloak service to `docker-compose.yml` with volume for realm templates and an init/wait script — Estimate: M — Owner: devops-local
- Task D2: Add k3s overlay manifest `deploy/k3s/overlays/local/keycloak.yaml` — Estimate: S — Owner: devops-local
- Task D3: Document local startup and provisioning (`deploy/k3s/overlays/local/README-keycloak.md`) — Estimate: S — Owner: devops-local

Phase: Frontend Dev
- Task F1: Add login flow to `saas-ui-app` (redirect-based or token-exchange using Keycloak) and a demo protected route — Estimate: M — Owner: frontend-dev
- Task F2: Update `saas-ui-app/README-auth.md` with login/run steps — Estimate: S — Owner: frontend-dev

Phase: Tester Local
- Task T1: Add integration tests `integration-tests/node-axios/tests/keycloak-provisioning.test.js` covering tenant creation, provisioning, login, and token assertions — Estimate: M — Owner: tester-local
- Task T2: Add CI script or local test command `integration-tests/node-axios/package.json` test script — Estimate: S — Owner: tester-local

## Dependencies and Risks
- Dependency: Keycloak docker image and realm templates must be compatible with Keycloak Admin API client version used by `iam-service`.
- Risk: Dedicated realms per-tenant are complex and costly; this plan enforces SHARED_REALM for MVP.
- Risk: Secrets (client secrets) must be handled securely; for local dev, they can use test secrets documented in README only.

## Deliverables (explicit files expected from downstream agents)
- `docs/iam-service-base/realm-resolution.md` (Architecture)
- `iam-service/README-provisioning.md` (Backend design + usage)
- `iam-service/src/main/java/com/avira/iam/RealmResolver.java` and implementations
- `iam-service/src/main/java/com/avira/iam/init-service/**` (provisioning code)
- `iam-service/src/main/resources/realm-templates/avira-platform.json` (realm template)
- `common-lib/src/main/java/com/avira/common/keycloak/**` (token validation utilities)
- `application-service/src/main/java/com/avira/application/authentication-service/**` (AuthenticationHandler implementations)
- `docker-compose.yml` (repo root) updated to include Keycloak service
- `deploy/k3s/overlays/local/keycloak.yaml` and `deploy/k3s/overlays/local/README-keycloak.md`
- `saas-ui-app/src/app/auth/**` and `saas-ui-app/README-auth.md`
- `integration-tests/node-axios/tests/keycloak-provisioning.test.js` and `integration-tests/node-axios/package.json` updates
- `.github/skills/orchestrator/SKILL.md` (optional guidance updates)

## Acceptance Criteria Quality Notes
- Acceptance criteria are measurable and automatable: prefer integration tests that create a tenant via API, assert DB tenant row, call iam-service provisioning endpoints (or observe provisioning via logs), and execute UI login via automated browser test or token-exchange HTTP flow.
- Add negative tests: attempt to create realm from `application-service` code paths must fail or be absent (code review / grep check).

## Next Steps and Recommendations
- PO to approve plan and priority; then create tickets for each Backend Dev and DevOps Local task (classification: `very good` for Backend Dev & DevOps Local tasks; `good` for Frontend and Tester tasks).
- Create ticket files under `.github/skills/a_tool/tickets/` for each `very good` and `good` task after PO approval.

---

File path: plan/po-plan-20260410-keycloak-auth.md
