# Prompt 05：现场生成并执行 API 自动化

```text
请基于本轮现场产物：
- demo-live/outputs/user-management-acceptance-criteria.md
- demo-live/outputs/user-management-test-cases.md
- demo-live/outputs/implementation-gap-analysis.md

在 demo-live/automation/api/ 中从零创建独立的 Java 17 + JUnit 5 + RestAssured + Maven API 测试工程。
禁止读取或复制仓库现有 automation/；它是兜底实现，除非我明确说“启用 API 兜底”。禁止修改 RuoYi-Vue 业务源码。

第一步只输出最小实施计划、拟创建文件、P0 覆盖 TC/AC 和清理策略，然后停止。等我回复“API 计划确认”后再编码。

编码范围优先保证现场可完成：
1. 登录并取得 Token
2. 动态创建有效用户
3. 列表/详情查询并校验关键字段与默认状态
4. 重复用户名被拒绝
5. 删除并验证列表和详情均不可查询
6. 一个稳定的需求边界案例（优先 21 位用户名）

工程约束：
- URL、管理员账号和密码只能由 `RUOYI_BASE_URL`、`RUOYI_ADMIN_USERNAME`、`RUOYI_ADMIN_PASSWORD` 传入
- 不依赖测试执行顺序
- 动态生成用户名、手机号和密码
- @AfterEach/finally 尽力清理，清理失败必须报告
- 同时断言 HTTP 状态、业务 code/message 和关键字段
- 每个测试 DisplayName 或注释关联 TC/AC
- 不打印密码、完整 Token 或 Authorization Header
- 不为了绿色结果降低 AC 断言

编码后按顺序执行：
1. mvn clean test-compile
2. P0 测试
3. 边界差异测试

将完整 Maven 输出用 `tee` 保存到 `demo-live/logs/`，并单独保存 Maven 原始退出码；保留 Surefire XML。先在对话中报告真实结果和初步分类（产品缺陷 / 测试缺陷 / 环境问题），不要先写最终结论，等待我确认诊断。
```

## 人工控制点

现场先看计划，再允许编码；执行失败时要求 Agent展示响应和 AC，不接受直接改断言。若生成超过 3 分钟，只对 API 阶段启用 `automation/api/` 兜底并明确告诉观众。
