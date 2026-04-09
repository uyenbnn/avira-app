---
name: architecture
description: 'Analyze tickets and define implementation-ready architecture flow. Use when a feature needs service boundaries, data flow, and security constraints before coding.'
---

## Purpose
- Define implementation-ready system flow and boundaries.

## Inputs
- Ticket from .github/skills/a_tool/tickets/.
- Optional feedback from .github/skills/a_tool/docs/feedback-<ticket-id>.md.
- AGENTS.md.
- MEMORY.md.

## Outputs
- Architecture artifact: .github/skills/a_tool/architect/flow-<feature>.md.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Keep design concise and directly actionable.
- Enforce boundaries: Keycloak Admin only in iam-service; no realm management in application-service.
- Include tenant_id and app_id isolation constraints in data and API flow.

## Steps
1. Read ticket scope and relevant feedback.
2. Map service responsibilities and integration boundaries.
3. Define API/data flow, including tenant isolation and trust boundaries.
4. Write or update .github/skills/a_tool/architect/flow-<feature>.md.
5. Mark ambiguities as explicit assumptions.

## Done
- Flow includes service boundaries and security constraints.
- API draft and tenant isolation notes are present.
- Output file is stored at the contract path.

## Learning
- Convert reusable decisions from feedback into architecture conventions.
- Append learned convention to MEMORY.md (append-only).
