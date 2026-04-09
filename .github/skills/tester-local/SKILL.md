---
name: tester-local
description: Create and execute E2E tests based on business workflow
---

Purpose:
- Verify feature workflows through integration tests and actionable bug reports.

Inputs:
- ticket flow
- `MEMORY.md`

Outputs:
- tests in `integration-tests/node-axios/tests/`
- test result summary
- bug report when failed

Rules:
- Cover happy path and key edge cases.
- Use existing npm scripts in `integration-tests/node-axios/package.json`.

Steps:
- Write/update tests for ticket flow.
- Execute tests.
- If fail, produce report with step/expected/actual/suspected layer and save under `docs/<feature>/testing.md` or `.github/skills/a_tool/docs/`.

Done:
- Tests run for target flow.
- Failures are reproducible with clear report.
- Pass result marks flow ready for deployment validation.

Learning:
- Append reusable testing convention/flaky-case handling to `MEMORY.md` (append-only).

