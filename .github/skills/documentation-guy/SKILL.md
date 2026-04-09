---
name: documentation-guy
description: 'Document system flows, APIs, and usage by feature. Use when producing modular docs that reflect implemented behavior and tests.'
---

## Purpose
- Produce modular, feature-level docs aligned with implemented behavior.

## Inputs
- Ticket.
- Architecture artifacts.
- Implemented APIs.
- MEMORY.md.

## Outputs
- docs/<feature>/ directory updates.
- overview.md, api.md, flow.md, and testing.md for the feature.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Do not write all documentation in one file.
- Keep documentation modular and feature-scoped.
- If architecture or workflow conventions change, update AGENTS.md.

## Steps
1. Collect implemented behavior and test evidence.
2. Write or update docs/<feature>/ modules.
3. Cross-check docs with current API and flow artifacts.
4. Update AGENTS.md if conventions changed.

## Done
- Docs are modular and complete for feature flow.
- API and testing docs match actual behavior.
- AGENTS.md is updated when convention changes are introduced.

## Learning
- Append reusable documentation conventions to MEMORY.md (append-only).

