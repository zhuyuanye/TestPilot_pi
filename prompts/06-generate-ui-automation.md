# Prompt 06：现场生成并执行 Vue UI 自动化

```text
请基于本轮现场产物实现 TC-001：
- demo-live/outputs/user-management-acceptance-criteria.md
- demo-live/outputs/user-management-test-cases.md
- demo-live/outputs/implementation-gap-analysis.md

在 demo-live/automation/ui/ 中从零创建 Java 17 + JUnit 5 + Playwright Java + Chromium 的独立 Maven 工程。
禁止读取或复制仓库现有 automation/ui；它只用于兜底。禁止修改 RuoYi-Vue 业务源码。

第一步只输出计划、页面定位策略、断言点、清理策略和证据策略，然后停止。等我回复“UI 计划确认”后再编码。

必须实现：
1. 真实 Vue 页面登录→新增→搜索确认→删除→确认消失
2. 只从 `RUOYI_UI_URL`、`RUOYI_BASE_URL`、`RUOYI_ADMIN_USERNAME`、`RUOYI_ADMIN_PASSWORD` 读取地址和凭据
3. 支持 HEADLESS、SLOW_MO，现场默认有头慢动作
4. 使用角色、文本、placeholder 和稳定作用域定位，避免全局 nth
5. 动态生成用户名、昵称、合法手机号和密码
6. 断言新增/查询/删除 HTTP 响应和用户可见结果
7. @AfterEach 通过 API 兜底清理
8. 标注 TC-001 与 AC-01、AC-19～AC-22

证据必须随真实执行生成，不能手写：
- 每次执行前清理旧证据
- 开启 Playwright tracing：screenshots、snapshots、sources
- 新增并查询成功后保存 `01-user-created-and-found.png`
- 删除并确认消失后保存 `02-user-deleted.png`
- 失败现场保存 `failure-<timestamp>.png`
- 结束时保存 `trace.zip`
- Surefire XML、Trace 和截图位于本轮 demo-live 目录
- 自动生成 Markdown 结果，列出每个证据的相对路径、大小和 SHA-256
- 将完整 Maven 输出和原始退出码保存到 `demo-live/logs/`
- Trace 可能包含敏感输入，不提交公开仓库

实现后先无头执行一次，再提供有头慢动作命令。展示实际生成的目录、截图和 trace.zip，不只口头声明成功。等待我确认后再进入最终报告。
```

## 人工控制点

现场打开两张截图，并用 Playwright Trace Viewer 查看至少一个登录后的步骤、一次 POST 新增请求和一次 DELETE 请求。
