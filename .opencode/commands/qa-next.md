---
description: Execute the next allowed step in the gated testing workflow
---
Load the `evidence-driven-testing` skill and follow it strictly.

Workspace: `$1`

If the workspace argument is empty, ask for it. Read `workflow-context.md` and `workflow-state.md`. Refuse to skip an unapproved gate.

Execute exactly one allowed transition for the current stage:

- For a non-automation stage at `not_started`, produce its candidate draft and stop at `draft_ready`.
- For API/UI automation at `not_started`, produce only the implementation/execution plan and stop at `plan_ready`.
- For API/UI automation at `approved_for_execution`, implement and run the approved scope, preserve raw evidence, produce a result candidate, and stop at `result_ready`.
- If the current stage is `blocked`, report only the blocker and available recovery/fallback choices.
- If a draft, plan, or result is already waiting for approval, do not redo or advance it.

At the end, report: current stage, files changed, evidence created, unresolved risks, and the exact human gate required next. Never self-approve.
