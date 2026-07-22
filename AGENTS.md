# TestPilot OpenCode Instructions

## Standard testing workflow

For requests that cover requirements analysis, acceptance criteria, test design, implementation comparison, API/UI automation, execution diagnosis, evidence auditing, or final test conclusions, load and follow the `evidence-driven-testing` skill.

Use the project commands under `.opencode/commands/` for the standard gated flow:

- `/qa-start`
- `/qa-next`
- `/qa-approve`
- `/qa-status`
- `/qa-evidence`
- `/qa-fallback`
- `/qa-review`

## Live demo isolation

- `demo-live/` is the only normal live-generation workspace.
- Root `outputs/`, `automation/`, and `presenter/checkpoints/` are fallback/reference material.
- Do not read fallback/reference material during a live stage unless the user explicitly approves `/qa-fallback`.
- Do not silently reuse historical test results, traces, screenshots, logs, or reports.

## Human gates

- Generated requirements analysis, AC, TC, implementation analysis, automation plans, execution diagnosis, and final review are candidates until explicit human approval.
- API and UI automation each require a plan approval and a result approval.
- Do not weaken assertions to match current implementation.
- Do not modify RuoYi-Vue product source as part of this black-box testing workflow.
- Never expose credentials, complete tokens, authorization headers, or sensitive trace contents.
