---
name: backend-dev
description: 'Implement backend APIs from ticket and architecture flow. Use when delivering Spring Boot service changes, OpenAPI updates, and backend feedback.'
---

## Purpose
- Implement backend changes from ticket and architecture flow.

## Inputs
- Ticket and architecture flow.
- AGENTS.md.
- CONVENTION.md.
- MEMORY.md.

## Outputs
- Source code changes.
- Updated OpenAPI for changed endpoints.
- Feedback file: .github/skills/a_tool/docs/feedback-<ticket-id>.md.

## Rules
- Preferred model strategy: agent auto.
- Enforce tenant scope in queries (tenant_id and app_id when applicable).
- Keycloak Admin API only in iam-service; application-service cannot provision realms.
- Keep IAM initialization behavior inside com.avira.iamservice.initservice.

## Steps
1. Implement controller/service/repository updates with DTO separation.
2. Apply service-boundary and tenant-isolation rules.
3. Update OpenAPI for endpoint behavior changes.
4. Run relevant unit tests for changed modules.
5. Write feedback with feedback, improvement, and next_step for architecture and po.

## Done
- Backend changes build and test for scope.
- OpenAPI reflects shipped behavior.
- Feedback artifact is created or appended at contract path.

## Learning
- Append reusable coding conventions to MEMORY.md (append-only).
