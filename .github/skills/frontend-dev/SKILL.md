---
name: frontend-dev
description: 'Build minimal UI based on OpenAPI for workflow validation. Use when implementing frontend flows that exercise backend APIs.'
---

## Purpose
- Build minimal functional UI to validate ticket workflows.

## Inputs
- Ticket flow and OpenAPI contract.
- AGENTS.md.
- MEMORY.md.

## Outputs
- UI changes in avira-ui-app/.
- Feedback file: .github/skills/a_tool/docs/feedback-<ticket-id>.md.

## Rules
- Preferred model strategy: agent auto.
- Prioritize function-first flows (register, login, create app, domain config).
- Keep UI simple, testable, and aligned with backend contract.

## Steps
1. Implement UI flow required by ticket.
2. Wire API calls from OpenAPI and backend contract.
3. Validate error states and essential edge paths.
4. Write feedback with feedback, improvement, and next_step for architecture and po.
5. Highlight API usability and flow-definition gaps.

## Done
- Ticket flow works in UI scope.
- API integration is aligned with current backend contract.
- Feedback artifact is created or appended at contract path.

## Learning
- Append reusable UI and API integration conventions to MEMORY.md (append-only).
