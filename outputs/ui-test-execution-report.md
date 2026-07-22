# Vue UI 自动化执行结果

> 由 `scripts/export-ui-result.py` 从 Maven Surefire XML 自动生成，时间：`2026-07-22T15:12:20+08:00`。

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
| shouldCreateFindAndDeleteUserFromVuePage | 通过 | 6.302 |  |

## 原始执行证据

| 文件 | 大小（字节） | SHA-256 |
|---|---:|---|
| `automation/ui/target/evidence/TC-001-20260722-151215-930/01-user-created-and-found.png` | 117259 | `189887ffa31cdeb2508b9482817cc5679e081826d41588a20964eabb672e231a` |
| `automation/ui/target/evidence/TC-001-20260722-151215-930/02-user-deleted.png` | 106407 | `05f6ed2ce165c9ec97de6cc854a4bba9282b254446190644672978880a03c02a` |
| `automation/ui/target/evidence/TC-001-20260722-151215-930/trace.zip` | 5111888 | `21bff81c23477c2e43bd43211fcd2ad539a3f9b9c17eaf01c797559e190f0dde` |

> Trace 可能包含页面快照、请求信息及输入操作，仅用于本地或受控环境，不提交公开仓库。

## 执行环境

- Vue 地址：`http://localhost:8081`
- API 地址：`http://localhost:8080`
- 框架：Playwright Java + JUnit 5 + Chromium
- 原始临时结果：`automation/ui/target/surefire-reports/`（不提交 Git）
- Trace 与截图：`automation/ui/target/evidence/TC-001-<timestamp>/`（不提交 Git）

## 执行命令

```bash
HEADLESS=true ./scripts/run-ui-tests.sh
```
