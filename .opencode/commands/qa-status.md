---
description: Show workflow stage, approvals, evidence, coverage, and blockers
---
Load the `evidence-driven-testing` skill.

Workspace: `$1`

Read the workflow context/state and report without modifying files:

1. Current stage and stage status
2. Approved artifacts and approval timestamps
3. Candidate or plan awaiting review
4. Requirement→AC→TC→automation→result traceability available so far
5. Raw logs/evidence present, with missing required evidence
6. Blockers and unresolved risks
7. Fallback entries and affected stages
8. Next allowed command

Do not infer a pass from Markdown summaries. Do not expose secrets or dump trace contents.
