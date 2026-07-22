# OpenCode × RuoYi-Vue 自动化测试实战

面向团队内部测试人员的 30 分钟演示材料。

## 演示目标

使用 OpenCode 完成一条可追溯的测试闭环：

```text
原始需求 → 需求分析与澄清 → AC → 测试用例
→ 实现对照 → 自动化脚本 → 执行诊断 → 测试结论
```

本演示不包含 CI/CD。API 自动化作为主线，并使用 Playwright Java 演示一个 Vue UI 核心场景。

## 目录

- `docs/requirements/`：提供给 OpenCode 的原始需求
- `presenter/`：讲师材料，不在第一阶段提供给 OpenCode
- `presenter/checkpoints/`：每个阶段的参考输出与现场兜底材料
- `prompts/`：按顺序执行的 OpenCode 提示词（需求到 API/UI 自动化）
- `runbook/`：30 分钟现场操作手册
- `outputs/`：OpenCode 后续生成 AC、测试用例和测试报告的位置

## 演示前准备

1. RuoYi-Vue 源码路径：`/Users/zhuyuanye/Documents/Code/RuoYi-Vue`。
2. 准备可运行的 MySQL、Redis、后端和前端环境。
3. 准备管理员测试账号，不使用生产数据。
4. 提前完整执行一次所有提示词和自动化脚本。
5. 保留稳定版本和失败场景，现场不依赖临时下载。

## 当前进度

- [x] 原始需求
- [x] 产品澄清口径
- [x] 分阶段 OpenCode 提示词
- [x] 30 分钟演示流程
- [x] 确定 RuoYi-Vue 3.9.2 及源码位置
- [ ] 现场通过 OpenCode 生成 AC 和测试用例
- [x] 创建独立 Java API 自动化工程
- [x] 创建 Playwright Java UI 核心流程
- [x] 准备可重复的需求差异测试
- [x] 完成 Maven 核心闭环执行验证
- [x] 验证需求差异用例可稳定失败并自动清理数据
- [x] 准备需求分析、AC、用例、实现对照和报告 checkpoints
- [ ] 由 OpenCode 在演示流程中生成并固化最终 outputs
