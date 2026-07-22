# Prompt 03：现场测试设计与分层

```text
请只基于本轮已经人工确认的：
- demo-live/outputs/01-requirement-analysis.md
- demo-live/outputs/user-management-acceptance-criteria.md

禁止读取仓库现有 outputs/、automation/ 和 presenter/checkpoints/，本阶段也不要查看 RuoYi-Vue 业务源码。

请先在对话中给出测试设计草案：
1. 每条用例使用 TC-编号并关联 AC
2. 包含前置条件、动态测试数据、步骤、客观预期和清理方式
3. 使用等价类、边界值、错误推测和状态迁移
4. 标注 P0/P1/P2及理由
5. 标注 API 自动化、UI 自动化或人工测试层
6. API 优先覆盖业务规则；UI 只保留一个核心用户流程
7. 数据必须唯一、可重复执行、失败后也能清理
8. 附“AC—TC—建议层”追踪矩阵，并指出未覆盖 AC

请特别说明为什么某些规则不放在 UI 层重复验证。
先展示草案并停止，等待人工评审。只有我回复“测试设计评审通过”后，才写入 demo-live/outputs/user-management-test-cases.md。不要生成代码。
```

## 人工控制点

现场选择 21 位用户名、非法手机号、当前管理员自删除三个例子，要求 Agent 解释边界、风险和执行层选择。
