---
name: test-ui-automation
description: 规划、生成并执行小范围核心 UI 自动化，要求稳定定位、动态数据、API 兜底清理、Playwright Trace、成功/失败截图和证据哈希。用于标准测试流程第六步。
license: MIT
compatibility: OpenCode 1.18.4 或更高版本
metadata:
  language: zh-CN
  stage: ui-automation
---

# 第六步：UI 自动化与可视证据

## 进入条件

- 已加载 `test-workflow-core` 并阅读证据规范。
- API 结果已人工批准，或人工明确允许独立进入 UI 阶段。

## 分层原则

UI 只验证用户可见核心旅程，不重复所有 API 等价类和边界。使用真实浏览器和真实测试环境。

## 第一门禁：计划

首次执行只能输出计划：

- 关联 TC/AC
- 页面流程和断言点
- 角色、文本、标签、placeholder 和稳定作用域定位策略
- 动态数据
- API 兜底清理
- HEADLESS/SLOW_MO 等运行参数
- Trace、成功截图、失败截图和机器结果
- 敏感信息风险

设置 `plan_ready` 并停止。

## 计划批准后

1. 在隔离 UI 测试工程实现，不修改业务源码。
2. URL、账号和密码只从环境变量读取。
3. 验证关键网络响应和用户可见状态。
4. `AfterEach/finally` 兜底清理动态数据。
5. 每次执行前清理旧证据或创建唯一运行目录。
6. 开启 Trace 的 screenshots、snapshots、sources。
7. 保存关键成功步骤截图和失败现场截图。
8. 结束时保存 `trace.zip`。
9. 保存完整日志、原始退出码和 XML/JSON。
10. 生成证据清单：相对路径、大小、SHA-256、TC/AC。
11. Trace 不提交公开仓库。

先无头验证稳定性，需要演示时再有头慢动作执行。

## 结果候选

写入 `drafts/06-ui-result.md`，状态设为 `result_ready`。

## 第二门禁：结果

人工必须看到真实浏览器行为，并打开本轮截图和 Trace 后才可批准。
