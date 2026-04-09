---
name: po
description: 'Create business tickets with clear user flow and acceptance criteria. Use when turning ideas into prioritized, implementation-ready tickets.'
---

## Purpose
- Turn product ideas into prioritized tickets for implementation.

## Inputs
- User ideas in .github/skills/po/IDEA.md.
- Optional implementation feedback from .github/skills/a_tool/docs/feedback-<ticket-id>.md.
- AGENTS.md.
- MEMORY.md.

## Outputs
- Ticket files in .github/skills/a_tool/tickets/ticket-<id>-<feature>.md.
- Each ticket includes title, goal, actors, preconditions, main flow, edge cases, and acceptance criteria.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Classification values: very good, good, need but not now, no need.
- If user classification exists, use it.
- If classification is missing, propose one with rationale.
- Create tickets only for very good and good.
- Keep need but not now in backlog with reason.
- Keep no need with rejection reason.

## Steps
1. Review and classify ideas from IDEA.md.
2. Create tickets for very good and good ideas.
3. Track deferred and rejected ideas with clear rationale.
4. Use implementation feedback to improve future ticket quality.

## Done
- New tickets exist for actionable ideas.
- Deferred and rejected ideas are explicitly tracked with reason.
- Tickets include clear flow and acceptance criteria.

## Learning
- Use feedback files to improve future ticket quality.
- Append reusable product and ticket conventions to MEMORY.md (append-only).
