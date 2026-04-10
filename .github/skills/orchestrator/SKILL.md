---
name: orchestrator
description: 'Coordinate end-to-end SDLC workflow from idea to deployment across Avira skills. Use when a task needs multi-skill orchestration and artifact handoff.'
---

## Purpose
- Coordinate multi-skill delivery from idea to validated implementation.

## Inputs
- User request.
- AGENTS.md.
- Existing artifacts under .github/skills/a_tool/.

## Outputs
- Coordinated artifacts in .github/skills/a_tool/.
- Product Owner process plan artifact in plan/.
- Validated delivery status with feedback and learning updates.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Do not force optional phases for small changes.
- Keep artifact paths explicit and pass artifacts between skills.
- Require Product Owner to create/update a readable process plan in `plan/` before implementation phases.
- Always close with feedback to learning updates.
- Routing policy to enforce:
	- backend-dev, frontend-dev, devops-local use agent auto.
	- all other skills use chatgpt 5.1 mini free version.

## Steps
1. Determine required skills from task scope.
2. Apply model routing per selected skill.
3. Run po first to create or update `plan/po-plan-<YYYYMMDD>-<short-topic>.md` for process visibility.
4. Run po for ticket definition when ticket is missing or unclear.
5. Run architecture for non-trivial implementation.
6. Run backend-dev and frontend-dev as needed, then test and iterate.
7. Run devops-local only when deployment workflow changes are required.
8. Run documentation-guy for feature documentation updates.
9. Ensure feedback artifacts and memory updates are completed.

## Done
- Required artifacts exist for selected phases.
- Product Owner process plan file exists in `plan/` and reflects phase flow.
- Test phase is complete (pass or documented failure with clear report).
- Feedback and memory updates are completed.

## Learning
- Append orchestration improvements to MEMORY.md (append-only).
