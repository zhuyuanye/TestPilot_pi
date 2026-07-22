---
description: 按状态机执行标准测试流程当前唯一允许的下一步
---
先加载 `test-workflow-core`。

工作区：`$1`

工作区为空时先询问。读取 `workflow-context.md` 和 `workflow-state.md`，根据 `current_stage` 加载且只加载对应阶段 Skill：

- `requirements` → `test-requirement-analysis`
- `acceptance` → `test-acceptance-criteria`
- `test-design` → `test-case-design`
- `implementation-analysis` → `test-implementation-analysis`
- `api-automation` → `test-api-automation`
- `ui-automation` → `test-ui-automation`
- `evidence-diagnosis` → `test-evidence-diagnosis`
- `final-review` → `test-final-review`

严格执行一次状态转换：

- 普通阶段为 `not_started`：生成候选稿，停在 `draft_ready`。
- API/UI 为 `not_started`：只生成计划，停在 `plan_ready`。
- API/UI 为 `approved_for_execution`：按批准计划编码并真实执行，保存原始证据，停在 `result_ready`。
- 当前为 `blocked`：只报告阻塞和可选恢复/兜底方案。
- 已有候选稿、计划或结果等待批准：不得重复执行或越过门禁。

结束时报告当前阶段、修改文件、生成证据、未决风险和下一道人工作业门禁。不得自行批准。
