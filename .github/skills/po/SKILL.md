---
name: po
description: 'Create business tickets with clear user flow and acceptance criteria. Use when turning ideas into prioritized, implementation-ready tickets.'
---

## Purpose
- Turn product ideas into prioritized tickets for implementation.

## Inputs
- User ideas in .github/skills/po/IDEA.md.
- Optional implementation feedback from .github/skills/a_tool/docs/feedback-<ticket-id>.md.
- User-provided plan artifact in .github/skills/a_tool/plan/.
- Architecture artifact(s) in .github/skills/a_tool/architect/.
- Backend and frontend feedback artifacts in .github/skills/a_tool/docs/.
- AGENTS.md.
- MEMORY.md.

## Outputs
- Ticket files in .github/skills/a_tool/tickets/ticket-<id>-<feature>.md.
- Process plan file in .github/skills/a_tool/plan/po-plan-<YYYYMMDD>-<short-topic>.md.
- Each ticket includes title, goal, actors, preconditions, main flow, edge cases, and acceptance criteria.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Classification values: very good, good, need but not now, no need.
- Always create or update a readable process plan file under `.github/skills/a_tool/plan/` so users can follow the workflow.
- If user classification exists, use it.
- If classification is missing, propose one with rationale.
- Create tickets only for very good and good.
- Keep need but not now in backlog with reason.
- Keep no need with rejection reason.

## Steps
1. Review and classify ideas from IDEA.md and user-provided plan.
2. Read architecture and backend/frontend feedback artifacts.
3. Create or update `.github/skills/a_tool/plan/po-plan-<YYYYMMDD>-<short-topic>.md` with scope, phases, dependencies, and outputs.
4. Create tickets for very good and good ideas.
5. Track deferred and rejected ideas with clear rationale.
6. Use architecture and implementation feedback to improve ticket quality.

## Done
- Process plan file exists in `.github/skills/a_tool/plan/`.
- New tickets exist for actionable ideas.
- Deferred and rejected ideas are explicitly tracked with reason.
- Tickets include clear flow and acceptance criteria.

## Learning
- Use feedback files to improve future ticket quality.
- Append reusable product and ticket conventions to MEMORY.md (append-only).
