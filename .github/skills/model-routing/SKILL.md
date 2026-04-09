---
name: model-routing
description: 'Route Avira skills to the preferred model strategy. Use when selecting execution model for backend-dev, frontend-dev, devops-local, and other .github/skills entries.'
argument-hint: 'Provide skill name(s) or task summary to resolve routing policy.'
---

## Purpose
- Provide a repeatable model-selection workflow for tasks executed via skills under .github/skills.
- Enforce preferred routing:
  - backend-dev, frontend-dev, devops-local -> agent auto
  - all other skills -> chatgpt 5.1 mini free version

## Inputs
- Requested task summary.
- Candidate skill(s) from .github/skills.
- Optional explicit override from user.

## Outputs
- Selected skill list.
- Selected model strategy per skill.
- Short rationale and fallback when conflict exists.

## Rules
- Preferred model strategy for this skill: chatgpt 5.1 mini free version.
- Source of truth for skills is .github/skills.
- Use exact skill names when matching:
  - backend-dev
  - frontend-dev
  - devops-local
- For the three skills above, default model strategy is agent auto.
- For every other skill in .github/skills, default model strategy is chatgpt 5.1 mini free version.
- If user explicitly requests a different model for a specific task, follow user request.
- If a task spans multiple skills, apply routing per skill, not per task.

## Steps
1. Detect applicable skills from task intent and .github/skills map.
2. Assign model strategy by rule:
   - backend-dev/frontend-dev/devops-local -> agent auto
   - others -> chatgpt 5.1 mini free version
3. Validate if user supplied explicit model overrides.
4. Return routing decision table before implementation.
5. Execute task using selected skill(s) and model strategy.
6. Record any routing exception in the summary.

## Decision Points
- If the task is ambiguous between two skills, choose the most specific skill and keep routing rule unchanged.
- If the task requires multiple skills, route each skill independently.
- If model availability is limited, use the closest available option and disclose the fallback.

## Done
- Every selected skill has a model strategy assignment.
- Routing decisions are explained in one concise table.
- Any override or fallback is explicitly noted.

## Learning
- Append stable routing changes to this file when team policy changes.
