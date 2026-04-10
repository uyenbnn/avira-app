---
name: tester-local
description: 'Create and execute local E2E and integration tests for business workflows. Use when validating feature behavior and producing actionable failure reports.'
---

## Purpose
- Verify feature workflows through integration tests and actionable bug reports.

## Inputs
- Ticket flow and acceptance criteria.
- MEMORY.md.

## Outputs
- Tests in integration-tests/node-axios/tests/.
- Test result summary.
- Bug report when failures occur.

## Rules
- Preferred model strategy: chatgpt 5.1 mini free version.
- Cover happy path and key edge cases.
- Use existing npm scripts in integration-tests/node-axios/package.json.
- Use axios for integration tests under integration-tests/node-axios/tests/.
- One use case per test file is mandatory.
- Failure reports must include ownership routing to backend or frontend.

## Steps
1. Write or update axios integration tests for ticket flow under integration-tests/node-axios/tests/.
2. Ensure one use case is isolated in each test file.
3. Execute tests and collect results.
4. If failures occur, produce report with step, expected, actual, suspected layer, and backend/frontend ownership.
5. Save report under docs/<feature>/testing.md or .github/skills/a_tool/docs/.
6. Return bug report artifact paths for fix-loop orchestration.

## Done
- Tests run for target flow.
- Failures are reproducible with clear report.
- Passing result marks flow ready for deployment validation.
- Bug ownership routing is explicit when failures exist.
- Test files follow one-usecase-per-file structure.

## Learning
- Append reusable testing and flaky-case handling conventions to MEMORY.md (append-only).

