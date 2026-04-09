# Skills Map

This index helps pick the right skill quickly.

Canonical `SKILL.md` schema (keep concise):
- `Purpose`
- `Inputs`
- `Outputs`
- `Rules`
- `Steps`
- `Done`
- `Learning`

Shared workflow artifacts:
- Ideas inbox: `.github/skills/po/IDEA.md`
- Tickets: `.github/skills/a_tool/tickets/`
- Architecture flow: `.github/skills/a_tool/architect/`
- Cross-agent feedback: `.github/skills/a_tool/docs/feedback-<ticket-id>.md`
- Agent memory: `.github/skills/<agent>/MEMORY.md` (append new conventions)

- `po`: create business ticket artifacts in `.github/skills/a_tool/tickets/`.
- `architecture`: define system/data/API flow in `.github/skills/a_tool/architect/`.
- `backend-dev`: implement Spring Boot changes with AGENTS service boundaries and tenant isolation.
- `frontend-dev`: implement minimal Angular UI in `avira-ui-app/`.
- `tester-local`: create/run integration tests in `integration-tests/node-axios/tests/`.
- `devops-local`: update local/prod deployment scripts in `scripts/` and k3s manifests in `deploy/k3s/`.
- `documentation-guy`: write modular feature docs in `docs/<feature>/` and update `AGENTS.md` when conventions change.
- `orchestrator`: coordinate end-to-end flow and pass artifacts between skills.

Learning rule:
- After task completion, each skill appends reusable conventions/lessons to its own `MEMORY.md`.
- `backend-dev` and `frontend-dev` must submit feedback to `architecture` and `po` using the feedback file contract.

