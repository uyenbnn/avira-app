---
name: orchestrator
description: Execute full SDLC workflow from idea to deployment using sub-agents
---

Purpose:
- Coordinate multi-skill delivery from idea to validated implementation.

Inputs:
- user request
- `AGENTS.md`

Outputs:
- coordinated artifacts in `.github/skills/a_tool/`
- validated delivery status with feedback/learning updates

Rules:
- DO NOT force optional phases for small changes.
- ALWAYS keep artifact paths explicit and pass artifacts between skills.
- ALWAYS close with feedback -> learning updates.

Steps:
- Run PO when ticket is missing/unclear; PO classifies `IDEA.md` and creates tickets only for `very good`/`good`.
- Run architecture for non-trivial work and store flow in `.github/skills/a_tool/architect/`.
- Run backend implementation; require feedback file at `.github/skills/a_tool/docs/feedback-<ticket-id>.md`.
- Run unit tests for changed modules.
- Run deploy/devops updates only when needed.
- Run frontend when ticket requires UI; append feedback to same ticket feedback file.
- Run tester and loop backend/frontend fixes until tests pass.
- Run documentation update in `docs/`.
- Trigger learning loop: architecture + PO consume feedback; all involved skills append `MEMORY.md`.

Done:
- Required artifacts exist for selected phases.
- Test phase is complete (pass or documented failure with clear report).
- Feedback and memory updates are completed.

Learning:
- Append orchestration improvements to `MEMORY.md` (append-only).
