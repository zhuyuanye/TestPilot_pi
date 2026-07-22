---
description: Record a human gate decision and advance the testing workflow safely
---
Load the `evidence-driven-testing` skill and follow it strictly.

Workspace: `$1`
Human review note: `$2`

This command represents a human gate decision, not agent self-approval. If the workspace is missing, ask for it. If the review note is empty, ask the user to state what was reviewed and approved.

Read the current state and candidate/plan/result. Before approval, summarize:
- current stage and gate type
- artifact or plan being approved
- unresolved blockers and risks
- whether any fallback was used

Refuse approval if blocking requirement questions remain, required evidence is missing, secrets are exposed, or the requested transition violates the skill state machine.

If valid:
1. Record the human note, timestamp, and current stage in `workflow-state.md` or an append-only approval log.
2. For a draft/result gate, promote the approved artifact from `drafts/` to `outputs/`, mark the stage approved, and advance to the next stage as `not_started`.
3. For an API/UI plan gate, keep the current stage and change status from `plan_ready` to `approved_for_execution`.
4. Do not execute the next stage in this command.

Report the next allowed command, normally `/qa-next $1`.
