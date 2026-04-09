---
name: po
description: Create business tickets defining user flow and requirements
---

Purpose:
- Turn product ideas into prioritized tickets for implementation.

Inputs:
- user ideas in `.github/skills/po/IDEA.md`
- optional implementation feedback from `.github/skills/a_tool/docs/feedback-<ticket-id>.md`
- `AGENTS.md`
- `MEMORY.md`

Outputs:
- `.github/skills/a_tool/tickets/ticket-<id>-<feature>.md`
- include: title, goal, actors, preconditions, main flow, edge cases, acceptance criteria.

Rules:
- Classification values: `very good`, `good`, `need but not now`, `no need`.
- If user already classifies an idea, PO uses that classification.
- If classification is missing, PO proposes one with rationale.
- Create tickets only for `very good` and `good`.
- Keep `need but not now` in backlog with reason.
- Keep `no need` with rejection reason.

Steps:
- Review/classify ideas from `IDEA.md`.
- Create tickets for `very good` and `good`.
- Keep deferred/rejected ideas with clear rationale.
- Consume implementation feedback to improve new tickets.

Done:
- New tickets exist for actionable ideas.
- Deferred/rejected ideas are explicitly tracked with reason.
- Ticket quality includes clear flow and acceptance criteria.

Learning:
- Use feedback files to improve future ticket quality.
- Append reusable product/ticket convention to `MEMORY.md` (append-only).
