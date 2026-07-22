# Execution Evidence Contract

## Raw evidence hierarchy

Prefer evidence in this order:

1. Machine-readable runner output: JUnit/Surefire XML, JSON report, TAP, etc.
2. Original process exit code stored separately from piped command output.
3. Complete execution log.
4. API response facts, browser trace, screenshots, video, or server logs.
5. Generated Markdown summary.

A Markdown summary alone cannot prove execution.

## Command capture

When piping through `tee`, preserve the tested process exit code rather than the `tee` exit code. In Bash:

```bash
<test-command> 2>&1 | tee <workspace>/logs/<run>.log
status=${PIPESTATUS[0]}
printf '%s\n' "$status" > <workspace>/logs/<run>.exit-code
```

Record the exact command without embedding credentials.

## Artifact manifest

For each artifact, record:

- Relative path
- Type/purpose
- Byte size
- SHA-256
- Creation time
- Related TC/AC where applicable

Example:

```text
evidence/run-20260101/trace.zip | ui-trace | 431245 bytes | sha256:... | TC-001
```

## API evidence

Capture non-sensitive facts:

- endpoint pattern and method
- HTTP status
- business status/code
- selected response fields
- dynamic entity identifier
- cleanup outcome

Do not log passwords, tokens, authorization headers, session cookies, or unnecessary personal data.

## UI evidence

Recommended minimum:

- Trace with snapshots/screenshots/sources
- Screenshot after entity creation/query verification
- Screenshot after deletion/state transition verification
- Failure screenshot when a step fails
- Test-runner XML and process exit code

Trace may contain credentials or request data. Keep it in ignored local storage or controlled artifact storage; do not publish it by default.

## Anti-fabrication challenge

Where safe, perform one controlled negative challenge without changing assertions, for example:

- wrong non-secret base URL
- stop a disposable service
- use a known-invalid test fixture

The test must turn red with a non-zero exit code and fresh failure evidence. Restore the environment and rerun. Classify the challenge as an environment/control check, never as a product defect.

## Result states

- `passed`: assertion passed and raw evidence is present.
- `failed`: assertion executed and failed with raw evidence.
- `blocked`: execution could not reach the intended assertion.
- `skipped`: runner explicitly skipped the test.
- `unverified`: no sufficient raw evidence.
