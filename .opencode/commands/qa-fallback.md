---
description: Declare and apply a stage-scoped testing workflow fallback with audit trail
---
Load the `evidence-driven-testing` skill and read its fallback policy.

Arguments:
- blocked stage: `$1`
- fallback source path: `$2`
- workflow workspace: `$3`

Do not copy anything immediately. First report:
- why the current stage is blocked
- what files would be copied
- which secret/history patterns will be excluded
- what must be re-executed
- how the final report will label fallback-derived content

Wait for explicit human confirmation. After confirmation, copy only the approved stage material, excluding credentials and historical execution artifacts, and append the required entry to `<workspace>/FALLBACK_USED.md`. Never remove or rewrite earlier entries. Do not claim copied test results as current; re-execution remains mandatory.
