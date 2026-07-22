---
description: 从原始需求启动带人工门禁的标准测试流程
---
先加载 `test-workflow-core` 和 `test-requirement-analysis` 两个 Skill，并严格执行。

参数：
- 原始需求：`$1`
- 目标源码路径：`$2`
- 隔离工作区：`$3`

任何参数为空或含义不清时，只提问，不执行后续操作。

安全检查：
1. 确认需求文件存在或需求文本已提供。
2. 确认工作区不是产品源码目录，也不是历史结果或兜底目录。
3. 工作区非空时先列出内容，未经同意不得删除或复用。
4. 本命令禁止读取产品实现、已有 AC/TC、自动化、报告和兜底材料。

初始化标准工作区、`workflow-context.md` 和 `workflow-state.md`。记录源码路径但暂不读取源码。然后只执行需求分析阶段：生成 `drafts/01-requirement-analysis.md`，将状态设为 `draft_ready` 或 `blocked`，停下来等待产品澄清和人工评审。不得自行批准或生成 AC。
