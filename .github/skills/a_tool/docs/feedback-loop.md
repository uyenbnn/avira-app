# Feedback Loop Contract

## File Path
- `.github/skills/a_tool/docs/feedback-<ticket-id>.md`

## Producers
- `backend-dev`
- `frontend-dev`

Write mode:
- One file per ticket.
- Each producer appends its own section (`## backend-dev`, `## frontend-dev`) to avoid overwrite.

## Consumers
- `architecture`
- `po`

## Required Sections
- ticket_id
- source_agent
- feedback
- improvement
- next_step
- impact_scope (api|flow|ux|security|data|deployment)

## Example
- ticket_id: 012
- source_agent: backend-dev
- feedback: Tenant ownership rule was unclear for update endpoint.
- improvement: Add explicit owner resolution rule in architecture flow.
- next_step: PO adds acceptance criterion for owner mismatch case.
- impact_scope: flow,data


