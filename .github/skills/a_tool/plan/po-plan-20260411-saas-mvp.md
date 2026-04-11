# Product Owner Plan: SaaS Platform MVP (2026-04-11)

## Scope and Assumptions
- Deliver a shippable MVP for the SaaS platform, supporting:
  - Platform Admin: full monitoring and permissions
  - Business User: registration, authentication, tenant auto-creation, CRUD for applications, app setup (domain, config, authentication modes)
  - End User: access to business user applications
- MVP must run locally (backend APIs, UI, integration tests, k3s deploy, UI validation)
- Respect Avira AGENTS/service split boundaries
- Assume local Keycloak and Postgres available via k3s
- Out of scope: advanced billing, external integrations, production hardening

## Selected Phases
1. Requirements & Architecture Review
2. Backend API Implementation (Spring Boot)
3. Frontend UI Implementation (Angular)
4. Integration Tests (Node/axios)
5. Local k3s Deployment (compose, manifests)
6. End-to-End Validation (UI + API)
7. Acceptance & Handoff

## Artifact Handoffs
- API: OpenAPI spec, Java controllers/services
- UI: Angular app, auth flows, CRUD screens
- Integration: Node/axios test suite
- Deployment: k3s manifests, compose files
- Docs: README, local run instructions

## Validation Approach
- Automated integration tests (Node/axios)
- Manual UI validation (login, CRUD, app setup)
- Local k3s deployment smoke test
- Acceptance demo with AGENTS boundary checks

## Acceptance Criteria
- All user roles can perform their MVP actions locally
- All APIs and UI flows covered by integration tests
- Local k3s deploy runs end-to-end (API, UI, Keycloak, Postgres)
- Documentation enables new dev to run MVP locally
- AGENTS/service split boundaries respected

---
Supersedes: None (first authoritative plan for this scope)
