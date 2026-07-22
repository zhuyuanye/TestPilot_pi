---
name: evidence-driven-testing
description: Run a gated, evidence-driven software testing lifecycle from raw requirements through clarification, acceptance criteria, test design, implementation analysis, API/UI automation, execution evidence, defect diagnosis, and human sign-off. Use when asked to plan, generate, execute, audit, or standardize an end-to-end testing workflow without treating existing implementation or generated reports as truth.
license: MIT
compatibility: OpenCode 1.18.4 or later; requires project read/write access and local test toolchains for execution stages
metadata:
  audience: testers
  workflow: requirements-to-evidence
---

# Evidence-Driven Testing

## Purpose

Turn raw requirements into a reproducible testing conclusion through explicit human gates and raw execution evidence.

This skill is technology-neutral. Adapt frameworks to the target project, but never weaken the process controls.

## Non-negotiable rules

1. Start from raw requirements. Do not inspect implementation before the implementation-analysis stage.
2. Treat generated content as a candidate until a human explicitly approves it.
3. Do not convert assumptions into requirements. Mark unresolved items as `待确认`.
4. Keep live outputs separate from reference, historical, or fallback artifacts.
5. Never treat current implementation as the source of expected behavior.
6. Do not modify product code unless the user separately authorizes a product fix after testing.
7. Do not weaken an assertion merely to obtain a green build.
8. Separate product defects, test defects, environment failures, and unverified risks.
9. A Markdown summary is not proof of execution. Verify raw logs, exit codes, test-runner XML, traces, screenshots, or equivalent artifacts.
10. Never expose passwords, tokens, authorization headers, production data, or sensitive traces.
11. The agent may recommend a verdict but cannot sign for product, development, or testing roles.

## Required context

Before starting, establish and persist:

- Requirement file or requirement text
- Target source path or repository
- Isolated workflow workspace
- Target environment and non-secret URLs
- Technology stack
- Intended API/UI/manual split
- Credential variable names, never credential values
- Whether fallback artifacts exist and where they are located

Write this to `<workspace>/workflow-context.md`.

## Workspace contract

Use this structure unless the project requires an equivalent:

```text
<workspace>/
├── workflow-context.md
├── workflow-state.md
├── inputs/
│   └── product-clarifications.md
├── drafts/
├── outputs/
├── automation/
│   ├── api/
│   └── ui/
├── logs/
├── evidence/
└── FALLBACK_USED.md
```

- `drafts/`: unapproved candidates.
- `outputs/`: human-approved artifacts only.
- `logs/` and `evidence/`: produced by real execution, not handwritten.
- Existing project reports or automation must not be copied into this workspace unless fallback is explicitly declared and recorded.

## Workflow state

Maintain `<workspace>/workflow-state.md` with:

```yaml
status: active
current_stage: requirements
stage_status: not_started
fallback_used: false
last_updated: <ISO-8601>
```

Allowed stages:

1. `requirements`
2. `acceptance`
3. `test-design`
4. `implementation-analysis`
5. `api-automation`
6. `ui-automation`
7. `evidence-diagnosis`
8. `final-review`

Allowed stage statuses:

- `not_started`
- `draft_ready`
- `plan_ready`
- `approved_for_execution`
- `result_ready`
- `approved`
- `blocked`

Do not skip a stage without recording the reason and obtaining human approval.

## Standard stage behavior

Read [stage contracts](references/stage-contracts.md) before executing a stage.

At every stage:

1. Read the state and only the inputs allowed for the current stage.
2. State what files and evidence will be read.
3. Produce a candidate under `drafts/` or a plan in the conversation.
4. Stop at the defined human gate.
5. On explicit human approval, record the reviewer note and timestamp.
6. Promote the approved artifact to `outputs/` or authorize implementation/execution.
7. Update `workflow-state.md` and advance only when the stage contract is satisfied.

## Automation stage double gate

API and UI automation require two approvals:

1. **Plan gate**: scope, files, assertions, data, cleanup, evidence, and risks.
2. **Result gate**: generated code, raw execution result, failure classification, and residual risk.

Transition:

```text
not_started → plan_ready
plan_ready --human approval→ approved_for_execution
approved_for_execution --implement/run→ result_ready
result_ready --human approval→ approved → next stage
```

## Evidence requirements

Read [evidence contract](references/evidence-contract.md) before running tests or writing conclusions.

Minimum evidence:

- Exact command
- Start/end time
- Original process exit code
- Complete log
- Test-runner machine-readable result when available
- Dynamic test-data identifier without secrets
- Cleanup result
- API response facts or UI trace/screenshots as applicable
- Relative artifact path, byte size, and SHA-256
- Source commit and non-secret environment identity

If evidence is missing, classify the result as `未证实`, not passed.

## Failure handling

For every failure, report:

1. Requirement/AC expectation
2. Test input
3. Actual HTTP response, page state, or runner error
4. Relevant implementation evidence
5. Reproducibility
6. Cleanup status
7. Classification:
   - product defect
   - test defect
   - environment/configuration failure
   - unresolved/unverified risk

Never change an expectation until a human supplies new requirement authority.

## Fallback handling

Read [fallback policy](references/fallback-policy.md) before using any existing output or automation.

Fallback must be:

- Scoped to one blocked stage
- Announced before use
- Copied without credentials or historical execution artifacts
- Re-executed in the current environment
- Recorded in `<workspace>/FALLBACK_USED.md`
- Clearly labeled in final reports

## Final review

The final review must show bidirectional traceability:

```text
Requirement ↔ Clarification ↔ AC ↔ TC ↔ Test method ↔ Raw result ↔ Defect
```

Distinguish:

- dynamically verified
- statically inferred
- not covered
- blocked
- fallback-derived

Leave product, development, and test sign-off fields unsigned.
