---
description: Start a gated evidence-driven testing workflow from raw requirements
---
Load the `evidence-driven-testing` skill and follow it strictly.

Arguments:
- requirement input: `$1`
- target source path: `$2`
- isolated workspace: `$3`

If any argument is empty or ambiguous, ask for it and do nothing else.

Safety checks:
1. Confirm the requirement input exists or was supplied as text.
2. Confirm the workspace is not the product source directory and is not a historical/fallback output directory.
3. If the workspace contains files, list them and ask before deleting or reusing anything.
4. Do not read product implementation, existing AC/TC, automation, reports, or fallback materials during this command.

Initialize the workspace contract and workflow state. Record source path but do not inspect it yet. Then execute only the `requirements` stage: analyze the raw requirement, write the candidate to `drafts/01-requirement-analysis.md`, mark the state `draft_ready` or `blocked`, and stop for human/product clarification and review. Do not self-approve or advance to acceptance criteria.
