---
name: test-workflow-core
description: 标准化测试流程的公共状态机、工作区、人工门禁、证据和兜底规则。执行需求分析、AC、测试设计、实现分析、API/UI 自动化、证据诊断或最终评审时必须先使用。
license: MIT
compatibility: OpenCode 1.18.4 或更高版本
metadata:
  language: zh-CN
  workflow: testing
---

# 标准测试流程公共规则

## 目标

用统一状态机管理“原始需求→测试结论”，保证每一步可追踪、可评审、可复现，且不会把 Agent 生成内容或历史报告当成事实。

## 不可违反的原则

1. 原始需求是预期行为的起点，当前实现不是需求来源。
2. Agent 产物默认为候选稿，未经人工批准不能进入正式输出。
3. 不得为了测试变绿而降低断言或修改需求口径。
4. 未获得单独授权时，不修改产品业务源码。
5. 现场产物与历史结果、参考材料、兜底工程必须隔离。
6. Markdown 报告不是执行证明，必须回到日志、退出码、XML/JSON、Trace、截图等原始证据。
7. 必须区分产品缺陷、测试脚本问题、环境问题和未证实风险。
8. 不输出密码、完整 Token、Authorization Header 或敏感 Trace 内容。
9. Agent 只能给出结论建议，不能代替产品、开发、测试签字。

## 标准工作区

```text
<workspace>/
├── workflow-context.md
├── workflow-state.md
├── approval-log.md
├── inputs/
├── drafts/
├── outputs/
├── automation/api/
├── automation/ui/
├── logs/
├── evidence/
└── FALLBACK_USED.md
```

- `drafts/`：待评审候选稿。
- `outputs/`：仅保存人工批准后的正式产物。
- `logs/`、`evidence/`：必须由真实执行产生。
- `approval-log.md`、`FALLBACK_USED.md`：只追加，不覆盖历史记录。

## 八个阶段

| 顺序 | 状态值 | 对应 Skill |
|---:|---|---|
| 1 | `requirements` | `test-requirement-analysis` |
| 2 | `acceptance` | `test-acceptance-criteria` |
| 3 | `test-design` | `test-case-design` |
| 4 | `implementation-analysis` | `test-implementation-analysis` |
| 5 | `api-automation` | `test-api-automation` |
| 6 | `ui-automation` | `test-ui-automation` |
| 7 | `evidence-diagnosis` | `test-evidence-diagnosis` |
| 8 | `final-review` | `test-final-review` |

## 状态文件

`workflow-state.md` 至少记录：

```yaml
status: active
current_stage: requirements
stage_status: not_started
fallback_used: false
last_updated: <ISO-8601>
```

阶段状态仅允许：

- `not_started`
- `draft_ready`
- `plan_ready`
- `approved_for_execution`
- `result_ready`
- `approved`
- `blocked`

禁止跳阶段。确需跳过时，必须记录原因和人工批准。

## 标准人工门禁

普通文档阶段：

```text
not_started → draft_ready → 人工批准 → approved → 下一阶段
```

API/UI 自动化阶段使用双门禁：

```text
not_started → plan_ready
plan_ready → 人工批准计划 → approved_for_execution
approved_for_execution → 编码并执行 → result_ready
result_ready → 人工批准结果 → approved → 下一阶段
```

`/qa-approve` 只记录批准和推进状态，不同时执行下一阶段。

## 公共证据与兜底规则

执行测试或形成结论前，读取：

- [证据规范](references/evidence-contract.md)
- [兜底规范](references/fallback-policy.md)

如果原始证据缺失，只能标记为“未证实”，不能标记通过。
