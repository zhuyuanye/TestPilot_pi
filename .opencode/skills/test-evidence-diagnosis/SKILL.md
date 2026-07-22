---
name: test-evidence-diagnosis
description: 从当前运行的日志、退出码、XML/JSON、Trace、截图和清理记录独立重算测试结果，核验报告声明并诊断产品、脚本、环境和未证实问题。用于标准测试流程第七步。
license: MIT
compatibility: OpenCode 1.18.4 或更高版本
metadata:
  language: zh-CN
  stage: evidence-diagnosis
---

# 第七步：证据核验与失败诊断

## 进入条件

- 已加载 `test-workflow-core` 并阅读证据规范。
- 至少存在本轮执行证据；没有证据时只能报告未证实或阻塞。

## 核验步骤

1. 列出实际命令、执行时间和原始退出码。
2. 从 XML/JSON 重算 tests、passed、failed、errors、skipped。
3. 检查日志是否完整且与本轮时间和源码版本一致。
4. 检查 Trace、截图和其他证据是否存在。
5. 计算每个证据的相对路径、字节大小和 SHA-256。
6. 核对动态数据和清理结果。
7. 将自动化方法映射到 TC 和 AC。
8. 对比已有 Markdown 声明，列出不一致。
9. 检查是否执行过受控负向挑战，以及测试是否真实变红。
10. 检查可能的敏感信息泄露，但不要打印敏感值。

## 每个失败的诊断格式

- AC 预期
- 测试输入
- 实际响应、页面或框架错误
- 实现证据
- 是否可复现
- 清理状态
- 分类：产品缺陷 / 测试脚本问题 / 环境问题 / 未证实风险

## 候选输出

写入：

- `drafts/07-test-execution-report.md`
- `drafts/07-defect-report.md`
- `drafts/07-traceability-matrix.md`

缺少原始证据的结论必须标记 `unverified`。

## 完成门禁

设置 `draft_ready` 并停止。人工确认统计、分类和缺陷范围后才可批准。
