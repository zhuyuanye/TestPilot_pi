---
description: Perform final read-only testing workflow review and prepare human sign-off
---
Load the `evidence-driven-testing` skill and follow its final-review rules.

Workspace: `$1`

Perform a read-only final review:

1. Verify bidirectional Requirement↔Clarification↔AC↔TC↔Method↔Raw result↔Defect traceability.
2. Separate dynamically verified, statically inferred, unverified, blocked, skipped, and fallback-derived claims.
3. Check that failures were not hidden by weakened assertions.
4. Check evidence integrity, cleanup status, residual test data, and secret exposure.
5. List uncovered AC and operational risks.
6. Recommend `通过`, `有条件通过`, or `不通过` with evidence-based reasons.
7. Produce empty product, development, and test sign-off fields.

The recommendation is advisory. Do not sign, self-approve, modify product code, or change prior evidence.
