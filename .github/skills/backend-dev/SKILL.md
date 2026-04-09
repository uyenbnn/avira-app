---
name: backend-dev
description: Implement backend APIs based on ticket and architecture
---

Purpose:
- Implement backend changes from ticket and architecture flow.

Inputs:
- ticket + architecture flow
- `AGENTS.md`
- `CONVENTION.md`
- `MEMORY.md`

Outputs:
- source code + updated OpenAPI
- feedback file: `.github/skills/a_tool/docs/feedback-<ticket-id>.md`

Rules:
- Enforce tenant scope in queries (`tenant_id`, `app_id` when applicable).
- Keycloak Admin API only in `iam-service`; `application-service` cannot provision realms.
- Keep IAM initialization behavior inside `com.avira.iamservice.initservice`.

Steps:
- Implement endpoints/services/repositories with DTO separation.
- Update OpenAPI for changed endpoints.
- Write feedback with `feedback`, `improvement`, `next_step` for `architecture` and `po`.
- Include concrete issues (constraints, flow gaps, endpoint usability).

Done:
- Buildable backend changes are present for scope.
- OpenAPI reflects behavior changes.
- Feedback artifact is appended/created at contract path.

Learning:
- Append reusable coding convention to `MEMORY.md` (append-only).
