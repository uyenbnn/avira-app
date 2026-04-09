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
- Validated delivery status with feedback and learning updates.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Do not force optional phases for small changes.
- Keep artifact paths explicit and pass artifacts between skills.
- Always close with feedback to learning updates.
- Routing policy to enforce:
	- backend-dev, frontend-dev, devops-local use agent auto.
	- all other skills use chatgpt 5.1 mini free version.

## Steps
1. Determine required skills from task scope.
2. Apply model routing per selected skill.
3. Run po when ticket is missing or unclear.
4. Run architecture for non-trivial implementation.
5. Run backend-dev and frontend-dev as needed, then test and iterate.
6. Run devops-local only when deployment workflow changes are required.
7. Run documentation-guy for feature documentation updates.
8. Ensure feedback artifacts and memory updates are completed.

## Done
- Required artifacts exist for selected phases.
- Test phase is complete (pass or documented failure with clear report).
- Feedback and memory updates are completed.

## Learning
- Append orchestration improvements to MEMORY.md (append-only).
