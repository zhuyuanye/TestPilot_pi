# Vue UI 自动化执行结果

> 由 `scripts/export-ui-result.py` 从 Maven Surefire XML 自动生成，时间：`2026-07-22T14:50:59+08:00`。

## 执行结论

| 项目 | 结果 |
|---|---|
| 总体状态 | 通过 |
| Tests run | 1 |
| Passed | 1 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |

## 场景追踪

- 测试用例：TC-001
- 验收标准：AC-01、AC-19、AC-20、AC-21、AC-22
- 流程：管理员登录 → 新增用户 → 搜索确认 → 删除用户 → 确认列表消失
- 数据清理：页面删除，并在 `@AfterEach` 中通过 API 兜底清理

## 测试明细

| 测试 | 状态 | 耗时（秒） | 失败摘要 |
|---|---|---:|---|
| shouldCreateFindAndDeleteUserFromVuePage | 通过 | 6.322 |  |

## 执行环境

- Vue 地址：`http://localhost:8081`
- API 地址：`http://localhost:8080`
- 框架：Playwright Java + JUnit 5 + Chromium
- 原始临时结果：`automation/ui/target/surefire-reports/`（不提交 Git）
- 失败截图：`automation/ui/target/screenshots/TC-001-user-management-<timestamp>.png`

## 执行命令

```bash
HEADLESS=true ./scripts/run-ui-tests.sh
```
