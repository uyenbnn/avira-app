# Keycloak Integration & Initialization Plan

## 1. Problem Statement and Scope
Integrate Keycloak as the identity provider for the SaaS MVP. Automate initialization to ensure the 'saas' realm and shared realms are created with required clients and users. This enables secure authentication and authorization for both admin and end-users.

**Scope:**
- Integrate Keycloak into the platform (local/dev/prod as needed).
- Auto-create 'saas' realm and at least one shared realm on startup/init.
- Each realm must have:
  - Confidential client (client-secret)
  - Public client (user authentication)
- Default admin user for 'saas' realm
- Anonymous user for both realms

## 2. User Stories
- As a DevOps engineer, I want Keycloak to be initialized with required realms, clients, and users so that environments are ready for development and testing.
- As an admin, I want to log in to the 'saas' realm with a default admin account after deployment.
- As a user, I want to authenticate via a public client in my realm.
- As a system, I want an anonymous user to exist for both realms for guest access scenarios.

## 3. Acceptance Criteria
- [ ] Keycloak is deployed and reachable in all target environments.
- [ ] On first startup, 'saas' and shared realms are present in Keycloak.
- [ ] Each realm contains:
    - [ ] One confidential client with generated/stored client-secret
    - [ ] One public client for user authentication
- [ ] 'saas' realm contains a default admin user with known credentials
- [ ] Both realms contain an anonymous user with restricted permissions
- [ ] Initialization is idempotent (safe to re-run)
- [ ] All configuration is documented and versioned (e.g., in realm-templates/)

## 4. Non-goals / Assumptions
- Not covering advanced Keycloak theming or custom flows.
- Not automating user/group sync with external IdPs.
- Assumes Keycloak is containerized and can be initialized via scripts or REST API.
- No UI changes required for this phase.

## 5. Implementation-ready Tasks by Phase

### Phase 1: Architecture
- Define realm, client, and user structure (Ownership: Architecture)
- Document initialization flow and config format (Ownership: Architecture)

### Phase 2: Backend/DevOps
- Author Keycloak initialization scripts or import JSON templates (Ownership: DevOps)
- Integrate init into deployment (docker-compose/k3s overlays) (Ownership: DevOps)
- Store realm/client/user templates in version control (Ownership: DevOps)
- Ensure idempotency and error handling (Ownership: DevOps)

### Phase 3: Testing
- Validate realms, clients, and users exist post-deploy (Ownership: Tester)
- Test admin and anonymous login flows (Ownership: Tester)

### Phase 4: Documentation
- Document setup, credentials, and troubleshooting (Ownership: Docs)

## 6. Risks and Dependencies
- Keycloak container/image availability
- Secrets management for client/admin credentials
- Initialization race conditions (if Keycloak not ready)
- Version drift between templates and deployed state

## 7. Artifact Paths by Phase
- Architecture: `docs/iam-service-base/realm-resolution.md`, `.github/skills/a_tool/plan/po-plan-20260411-keycloak-init.md`
- DevOps: `keycloak/realm-templates/saas-realm.json`, `keycloak/realm-templates/shared-realm.json`, `scripts/init-keycloak.sh`, `deploy/k3s/base/keycloak-deployment.yaml`
- Testing: `integration-tests/README.md`, test scripts if needed
- Docs: `docs/iam-service-base/overview.md`
