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
- `prompts/`：现场依次执行的 OpenCode 提示词（需求→证据→结论）
- `runbook/`：30 分钟现场操作手册
- `demo-live/`：现场实时生成工作区；除说明文件外不提交 Git
- `outputs/`：提前验证过的兜底输出，正常现场流程禁止 Agent 读取
- `automation/`：提前验证过的兜底自动化，正常现场代码生成在 `demo-live/automation/`
- `presenter/checkpoints/`：模型或环境故障时的讲师兜底材料

## 现场生成与兜底边界

演示开始前运行：

```bash
./scripts/prepare-live-demo.sh --reset
```

现场产生的分析、AC、用例、代码、Surefire XML、Trace、截图和报告均写入 `demo-live/`。只有单个阶段超时或发生环境故障时，才使用 `scripts/use-demo-fallback.sh <phase>`；脚本会记录 `demo-live/FALLBACK_USED.md`，禁止把兜底冒充现场生成。

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
- [x] 准备经过验证的 AC、测试用例和实现差异分析兜底
- [x] 准备独立 Java API 自动化兜底工程
- [x] 准备 Playwright Java UI 核心流程兜底工程
- [x] 准备可重复的需求差异测试
- [x] 完成 Maven 核心闭环执行验证
- [x] 验证需求差异用例可稳定失败并自动清理数据
- [x] 准备需求分析、AC、用例、实现对照和报告 checkpoints
- [x] 固化兜底 outputs（不作为现场默认输入）
- [x] 完成兜底 API、UI、自删除保护真实执行
- [x] 形成缺陷清单和人工评审门禁
- [ ] 完成一次严格计时的 30 分钟彩排

## 兜底工程验证入口

```bash
export RUOYI_BASE_URL=http://localhost:8080
export RUOYI_UI_URL=http://localhost:8081
export RUOYI_ADMIN_USERNAME='<test-admin>'
export RUOYI_ADMIN_PASSWORD='<test-password>'

./scripts/check-demo-env.sh
./scripts/run-api-tests.sh -Dtest=UserManagementP0ApiTest
./scripts/run-ui-tests.sh
./scripts/run-all-tests.sh
```

完整 API 验收会如实报告当前需求差异，因此整体返回非零状态属于预期测试结论，不应通过降低断言变成绿色。

上述命令验证的是根目录兜底工程。现场应执行 Agent 在 `demo-live/automation/` 生成的工程，具体命令见 Runbook，并把报告写入 `demo-live/outputs/`。

兜底 UI 工程每次执行后会将 Surefire XML 转换为 `outputs/ui-test-execution-report.md`；Trace 和截图保留在 `automation/ui/target/evidence/`。现场工程必须在 `demo-live/` 生成自己的 XML、Trace、截图和 SHA-256 清单。
