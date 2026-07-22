# OpenCode 中文标准化测试流程：Skills 与 Commands

## 设计方式

整个流程不再由一个大 Skill 承担。公共状态机与每个业务阶段相互分离：

- `test-workflow-core` 负责公共规则、工作区、状态机、人工门禁、证据和兜底。
- 八个步骤各自对应一个独立中文 Skill。
- `/qa-next` 读取当前状态后，只加载当前阶段 Skill。

OpenCode 的 Skill 名称受规范限制，只能使用小写英文字母、数字和连字符；Skill 的描述、正文、规则和输出要求均为中文。

## Skills

| 步骤 | Skill | 中文职责 |
|---:|---|---|
| 公共 | `test-workflow-core` | 状态机、目录、人工门禁、证据规范、兜底规范 |
| 1 | `test-requirement-analysis` | 原始需求分析、歧义、假设、风险和产品澄清 |
| 2 | `test-acceptance-criteria` | Given-When-Then AC、自检和需求—AC追踪 |
| 3 | `test-case-design` | TC、优先级、测试方法、分层、数据和清理 |
| 4 | `test-implementation-analysis` | 需求与源码实现对照、文件和符号证据 |
| 5 | `test-api-automation` | API 计划门禁、编码执行、业务断言和原始证据 |
| 6 | `test-ui-automation` | UI 计划门禁、核心流程、Trace、截图和清理 |
| 7 | `test-evidence-diagnosis` | 重算测试结果、证据哈希、失败分类和缺陷 |
| 8 | `test-final-review` | 双向追踪、风险、结论建议和人工签字边界 |

文件位置：

```text
.opencode/skills/
├── test-workflow-core/
├── test-requirement-analysis/
├── test-acceptance-criteria/
├── test-case-design/
├── test-implementation-analysis/
├── test-api-automation/
├── test-ui-automation/
├── test-evidence-diagnosis/
└── test-final-review/
```

## Commands

| 命令 | 作用 |
|---|---|
| `/qa-start <需求> <源码> <工作区>` | 加载公共 Skill 和第一步 Skill，初始化并生成需求分析候选稿 |
| `/qa-next <工作区>` | 按状态映射表加载当前阶段 Skill，只执行一次合法转换 |
| `/qa-approve <工作区> <评审说明>` | 记录人工批准；不会同时执行下一阶段 |
| `/qa-status <工作区>` | 查看阶段、当前 Skill、门禁、覆盖、证据和阻塞 |
| `/qa-evidence <工作区>` | 加载证据诊断 Skill，独立核验原始结果 |
| `/qa-fallback <阶段> <来源> <工作区>` | 按公共兜底规范提出方案，二次确认后复制并留痕 |
| `/qa-review <工作区>` | 加载最终评审 Skill，执行只读收口 |

## 本项目启动示例

```text
/qa-start docs/requirements/user-management.md /Users/zhuyuanye/Documents/Code/RuoYi-Vue demo-live
```

该命令只允许加载：

- `test-workflow-core`
- `test-requirement-analysis`
- 原始需求

产品澄清并更新候选稿后：

```text
/qa-approve demo-live "需求规则、待确认项和产品澄清已评审，无阻塞项"
```

生成 AC：

```text
/qa-next demo-live
```

此时 `/qa-next` 会根据 `current_stage: acceptance` 加载 `test-acceptance-criteria`，不会加载后续实现或自动化 Skill。

人工评审后：

```text
/qa-approve demo-live "AC 来源、边界、权限和数据一致性已评审"
```

后续继续交替：

```text
/qa-next demo-live
/qa-approve demo-live "本阶段人工评审说明"
```

## API/UI 双门禁

API 和 UI Skill 都要求两次人工批准。

第一次 `/qa-next`：

```text
not_started → plan_ready
```

批准计划：

```text
/qa-approve demo-live "范围、断言、数据、清理和证据计划已确认"
```

再次执行：

```text
/qa-next demo-live
```

此时才允许编码和真实执行：

```text
approved_for_execution → result_ready
```

人工检查原始日志、退出码、XML、Trace/截图和清理结果后，再次 `/qa-approve` 才进入下一阶段。

## 状态与证据

随时查看：

```text
/qa-status demo-live
```

独立核验：

```text
/qa-evidence demo-live
```

证据核验 Skill 会重新读取机器结果、退出码和证据文件，不以 Markdown 摘要作为执行事实。

## 兜底

```text
/qa-fallback api-automation automation/api demo-live
```

第一次只展示阻塞原因、复制范围、排除项和重跑要求。人工再次确认后才复制，并追加：

```text
demo-live/FALLBACK_USED.md
```

## 最终评审

```text
/qa-review demo-live
```

最终 Skill 只读审查，区分动态、静态、未覆盖、阻塞、跳过、兜底和未证实，不代替团队签字。

## OpenCode 发现验证

修改 Skill/Commands 后重新启动 OpenCode。演示前执行：

```bash
opencode debug skill | grep -E 'test-workflow-core|test-requirement-analysis|test-final-review'
opencode debug config | grep -E 'qa-start|qa-next|qa-approve|qa-evidence'
```
