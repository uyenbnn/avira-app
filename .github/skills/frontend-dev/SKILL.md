---
name: frontend-dev
description: Build minimal UI based on OpenAPI for testing workflows
---

Purpose:
- Build minimal functional UI to validate ticket workflows.

Inputs:
- ticket flow + OpenAPI
- `AGENTS.md`
- `MEMORY.md`

Outputs:
- UI changes in `avira-ui-app/`
- feedback file: `.github/skills/a_tool/docs/feedback-<ticket-id>.md`

Rules:
- Prioritize function-first flows (register/login/create app/domain config).
- Keep UI simple and testable.

Steps:
- Implement UI flow required by ticket.
- Wire API calls from OpenAPI/backend contract.
- Write feedback with `feedback`, `improvement`, `next_step` for `architecture` and `po`.
- Highlight API usability and flow-definition gaps.

Done:
- Ticket flow works in UI scope.
- API integration is aligned with current backend contract.
- Feedback artifact is appended/created at contract path.

Learning:
- Append reusable UI/API integration convention to `MEMORY.md` (append-only).
