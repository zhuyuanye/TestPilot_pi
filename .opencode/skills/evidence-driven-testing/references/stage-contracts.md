# Stage Contracts

## 1. Requirements

Allowed input: raw requirement only.

Forbidden input: product source, existing AC/TC, automation, historical reports, fallback materials.

Candidate output: `drafts/01-requirement-analysis.md` containing explicit rules, ambiguities, assumptions, risks, prioritized questions, and blocked/non-blocked status.

Gate: human/product clarification and approval. Persist human answers in `inputs/product-clarifications.md`.

Approved output: `outputs/01-requirement-analysis.md`.

## 2. Acceptance

Allowed input: raw requirement, approved requirement analysis, human clarifications.

Candidate output: `drafts/02-acceptance-criteria.md` with atomic Given/When/Then criteria, source references, and requirement-to-AC matrix.

Gate: human reviews traceability, testability, ambiguity, duplication, omissions, and accidental implementation details.

Approved output: `outputs/02-acceptance-criteria.md`.

## 3. Test design

Allowed input: approved requirements and AC. Do not read implementation yet.

Candidate output: `drafts/03-test-cases.md` with TC IDs, AC links, priority, technique, preconditions, unique data, steps, objective expected results, cleanup, and API/UI/manual layer.

Gate: human reviews boundaries, equivalence classes, permissions, state consistency, cleanup, and uncovered AC.

Approved output: `outputs/03-test-cases.md`.

## 4. Implementation analysis

Allowed input: approved requirement/AC/TC plus target source.

Candidate output: `drafts/04-implementation-analysis.md` separating AC expectation, implementation evidence, and gap/dynamic-verification risk. Every implementation claim needs file and symbol evidence.

Gate: human opens and checks representative source locations.

Approved output: `outputs/04-implementation-analysis.md`.

Static analysis cannot mark a TC passed.

## 5. API automation

First pass output: implementation plan listing files, TC/AC scope, environment contract, assertions, unique data, cleanup, evidence, and risks. Stop at plan gate.

After approval: implement in `automation/api/`, compile, run focused P0 tests, then selected boundaries. Persist logs and original exit codes.

Candidate result: `drafts/05-api-result.md` derived from raw artifacts.

Result gate: human reviews failures against AC and actual responses.

Approved output: `outputs/05-api-result.md`.

## 6. UI automation

Keep UI scope intentionally small unless the user approves expansion. Prefer one core journey while API tests cover business equivalence classes and boundaries.

First pass output: plan with stable locator strategy, assertions, data, API cleanup, trace/screenshots, and security risks. Stop at plan gate.

After approval: implement in `automation/ui/`, execute headless for stability and headed for demonstration when required.

Minimum UI evidence: runner XML, complete log, exit code, trace, success checkpoints, failure screenshot, artifact hashes.

Candidate result: `drafts/06-ui-result.md`.

Approved output: `outputs/06-ui-result.md`.

## 7. Evidence and diagnosis

Allowed input: approved AC/TC, current source evidence, current-run raw artifacts. Historical summaries are not authoritative.

Recompute totals from machine-readable artifacts. Verify artifact existence, size, hash, timestamps, and command exit codes. Map methods to TC/AC. Diagnose each failure.

Candidate outputs:

- `drafts/07-test-execution-report.md`
- `drafts/07-defect-report.md`
- `drafts/07-traceability-matrix.md`

Gate: human confirms classification and scope.

Approved versions go to `outputs/`.

## 8. Final review

Read-only audit. Check bidirectional traceability, dynamic versus static claims, secret exposure, cleanup, fallback disclosure, and uncovered risks.

Output: `drafts/08-final-review.md`.

Human reviewers decide the verdict and sign-off. The agent does not self-approve.
