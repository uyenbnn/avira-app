---
name: architecture
description: Analyze ticket and define system architecture and flow
---

Purpose:
- Define implementation-ready system flow and boundaries.

Inputs:
- ticket from `.github/skills/a_tool/tickets/`
- optional feedback from `.github/skills/a_tool/docs/feedback-<ticket-id>.md`
- `AGENTS.md`
- `MEMORY.md`

Outputs:
- `.github/skills/a_tool/architect/flow-<feature>.md`

Rules:
- Keep design concise and directly actionable.
- Enforce boundaries: Keycloak Admin only in `iam-service`; no realm management in `application-service`.
- Include tenant/app isolation constraints in data flow.

Steps:
- Read ticket and relevant feedback.
- Write/update architecture flow artifact.
- Mark any ambiguity as explicit assumptions.

Done:
- Flow includes service boundaries and security constraints.
- API draft and tenant isolation notes are present.
- Output path is correct.

Learning:
- If feedback exists, convert reusable decisions to architecture conventions.
- Append learned convention to `MEMORY.md` (append-only).
