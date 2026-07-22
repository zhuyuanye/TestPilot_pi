---
description: Independently audit raw test evidence and detect unsupported claims
---
Load the `evidence-driven-testing` skill and read its evidence contract.

Workspace: `$1`

Perform a read-only evidence audit:

1. Locate exact commands, complete logs, original exit-code files, runner XML/JSON, traces, screenshots, and cleanup records.
2. Recompute test totals from machine-readable artifacts.
3. Calculate relative path, byte size, and SHA-256 for evidence files.
4. Compare report claims with raw evidence and list every mismatch.
5. Distinguish passed, failed, blocked, skipped, and unverified.
6. Check whether artifacts appear current for the recorded source commit and environment.
7. Check for likely secret exposure without printing sensitive values.
8. Report whether a controlled negative challenge exists and whether it produced fresh failure evidence.

Do not modify test code, assertions, raw evidence, or approved reports. Write an audit candidate only if the user requests a file; otherwise report in the conversation.
