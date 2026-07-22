# OpenCode 标准化测试流程：Skill 与 Commands

## 组成

### Skill

```text
.opencode/skills/evidence-driven-testing/SKILL.md
```

该 Skill 定义通用的需求到证据测试闭环，包括：

- 阶段状态机
- 人工评审门禁
- API/UI 双重门禁
- 工作区隔离
- 原始执行证据契约
- 失败分类规则
- 兜底使用与审计规则
- 最终双向追踪和人工签字边界

OpenCode 可以根据任务自动加载，也可以由 Commands 明确要求加载。

### Commands

| 命令 | 作用 |
|---|---|
| `/qa-start <需求> <源码> <工作区>` | 初始化工作区，只执行原始需求分析并停在人工门禁 |
| `/qa-next <工作区>` | 根据状态机执行当前阶段唯一允许的下一步 |
| `/qa-approve <工作区> <评审说明>` | 记录人工批准；不会同时执行下一阶段 |
| `/qa-status <工作区>` | 只读查看阶段、门禁、覆盖、证据、阻塞和兜底状态 |
| `/qa-evidence <工作区>` | 独立核验日志、退出码、XML、Trace、截图和报告声明 |
| `/qa-fallback <阶段> <来源> <工作区>` | 先提出阶段级兜底方案，人工再次确认后才复制并留痕 |
| `/qa-review <工作区>` | 最终只读审查并生成待人工签字的建议 |

## 本项目启动示例

在项目根目录启动 OpenCode，然后执行：

```text
/qa-start docs/requirements/user-management.md /Users/zhuyuanye/Documents/Code/RuoYi-Vue demo-live
```

该命令只能读取原始需求，会：

1. 创建工作区结构；
2. 写入 `workflow-context.md` 和 `workflow-state.md`；
3. 生成需求分析候选稿；
4. 停止在产品澄清和人工评审门禁。

产品在对话中完成澄清后：

```text
/qa-approve demo-live "需求规则、待确认项和产品澄清已评审，无阻塞项"
```

进入下一阶段：

```text
/qa-next demo-live
```

审查候选 AC 后：

```text
/qa-approve demo-live "AC 来源、边界、权限和数据一致性已评审"
```

后续继续交替使用：

```text
/qa-next demo-live
/qa-approve demo-live "本阶段的人工评审说明"
```

## API/UI 双门禁示例

进入 API 或 UI 自动化阶段后，第一次 `/qa-next` 只能输出计划：

```text
not_started → plan_ready
```

人工批准计划：

```text
/qa-approve demo-live "自动化范围、断言、数据、清理和证据计划已确认"
```

此时状态为：

```text
approved_for_execution
```

再次执行：

```text
/qa-next demo-live
```

OpenCode 才能创建代码并真实执行，完成后停在：

```text
result_ready
```

人工检查原始证据后再次批准，流程才进入下一阶段。

## 随时查看状态

```text
/qa-status demo-live
```

重点显示：

- 当前阶段和状态
- 等待评审的候选稿/计划/结果
- 已批准产物与时间
- 当前追踪关系
- 原始证据缺口
- 阻塞和未覆盖风险
- 是否使用过兜底
- 下一条允许执行的命令

## 独立核验证据

```text
/qa-evidence demo-live
```

该命令不会修改测试或报告，而是重新：

- 从机器可读结果统计通过/失败；
- 检查完整日志和原始退出码；
- 计算 Trace、截图等证据的大小和 SHA-256；
- 对比 Markdown 声明与原始证据；
- 标记 `passed / failed / blocked / skipped / unverified`；
- 检查受控负向挑战是否真的使测试变红。

## 使用兜底

例如 API 阶段受阻：

```text
/qa-fallback api-automation automation/api demo-live
```

第一次执行只展示：

- 阻塞原因
- 将复制的文件
- 排除的凭据和历史证据
- 必须重新执行的内容
- 最终报告如何标记兜底

只有人工再次明确确认后才允许复制，并写入：

```text
demo-live/FALLBACK_USED.md
```

## 最终审查

```text
/qa-review demo-live
```

最终结果必须区分：

- 动态验证通过/失败
- 仅静态分析
- 未覆盖
- 环境阻塞
- 使用兜底
- 原始证据不足、未证实

OpenCode 只能给出建议，产品、开发、测试签字必须由人完成。

## 重新加载

项目新增或修改 Skill/Commands 后，重新启动 OpenCode。若当前版本支持资源热加载，也建议在正式演示前退出并重新进入，避免沿用旧命令缓存。

验证 Skill 是否被发现：

```bash
opencode debug skill | grep -A3 evidence-driven-testing
```

验证 Commands 是否进入解析后的配置：

```bash
opencode debug config | grep -E 'qa-start|qa-next|qa-approve|qa-evidence'
```
