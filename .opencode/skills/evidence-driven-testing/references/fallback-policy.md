# Fallback Policy

Fallback exists to keep a demonstration or time-boxed workflow moving; it must never impersonate live generation.

## Entry conditions

Use fallback only when one stage is blocked by:

- model/network failure
- generation exceeding the declared time budget
- unrecoverable local toolchain issue
- environment failure unrelated to product behavior

## Required procedure

1. Announce the blocked stage and reason.
2. Obtain human approval to use fallback.
3. Append an entry to `<workspace>/FALLBACK_USED.md`:
   - timestamp
   - stage
   - reason
   - source path and source commit/version
   - files copied
   - approver note
4. Exclude `.env`, secrets, build output, logs, runner XML, traces, screenshots, videos, and previous reports unless the explicit purpose is to show a labeled historical report.
5. Re-run copied automation in the current environment.
6. Mark all fallback-derived artifacts in traceability and final reports.

## Prohibited behavior

- silently reading fallback before live analysis
- replacing the entire workflow when only one stage is blocked
- carrying historical green results into a current-run report
- removing the fallback record
- presenting fallback as model-generated in the current session
