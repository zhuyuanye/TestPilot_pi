# 30 分钟现场演示 Runbook

## 1. 演示原则

本演示的主角是**现场过程**，不是预置结果：

```text
原始需求 → Agent 提问 → 人工澄清 → 候选 AC → 人工评审
→ 测试设计 → 实现取证 → 现场生成自动化 → 真实执行
→ Trace/截图核验 → 失败诊断 → 人工结论
```

正常流程的所有新产物写入 `demo-live/`。以下目录仅在明确宣布“启用兜底”后使用：

- `outputs/`
- `automation/`
- `presenter/checkpoints/`

不能提前把预置 AC、用例或代码放进 Agent 上下文，否则观众无法判断结果是否来自现场分析。

## 2. 演示前准备（不属于现场产出）

### 2.1 一次性技术准备

- RuoYi-Vue 3.9.2 后端：`http://localhost:8080`
- Vue 页面：`http://localhost:8081`
- 验证码关闭
- 专用、可恢复的管理员测试账号
- Java、Maven、OpenCode 1.18.4、Chromium 可用
- Maven 依赖和 Playwright Chromium 已缓存，现场不依赖下载

凭据只放在环境变量或被 Git 忽略的本地 `.env`，不出现在 Prompt、日志、截图名或 Git 中。

### 2.2 演示开始前 5 分钟

```bash
git status --short
./scripts/prepare-live-demo.sh --reset

export RUOYI_BASE_URL=http://localhost:8080
export RUOYI_UI_URL=http://localhost:8081
export RUOYI_ADMIN_USERNAME='<test-admin>'
export RUOYI_ADMIN_PASSWORD='<test-password>'

./scripts/check-demo-env.sh
```

确认现场工作区为空：

```bash
find demo-live -maxdepth 2 -type f -print
```

此时除 `README.md` 和 `SESSION.md` 外不应存在 AC、用例、代码或报告。

### 2.3 现场大纲

直接打开：

```bash
open presenter/live-demo-outline.html
```

这是培训开场单页，只介绍今天的主要内容和完整流程。浏览器全屏展示约 1 分钟后，切回 OpenCode、源码、终端和浏览器开始现场操作。

### 2.4 屏幕布局

- 左侧：OpenCode 对话和计划
- 右上：原始需求/Agent 新生成文件
- 右下：终端、测试日志和浏览器
- 不提前打开 checkpoint 或根目录 `outputs/`

## 3. 30 分钟时间线

| 时间 | 现场动作 | 必须让观众看到 | 人工控制点 |
|---|---|---|---|
| 00:00–02:00 | 说明目标、边界、清空 `demo-live` | 空工作区和基线 commit | 强调 Agent 不替代签字 |
| 02:00–05:00 | Prompt 01 需求分析 | Agent 从原始需求提出问题 | 指出一项遗漏或假设 |
| 05:00–07:30 | 讲师扮演产品逐项澄清 | 澄清前后结论变化 | 确认仍无阻塞项 |
| 07:30–10:00 | Prompt 02 生成候选 AC | 候选稿、自检、人工修正 | 输入“AC 评审通过”后才写文件 |
| 10:00–12:30 | Prompt 03 测试设计 | API/UI/人工分层和边界值 | 输入“测试设计评审通过” |
| 12:30–15:30 | Prompt 04 实现分析 | Agent 打开真实 Java/Vue 文件取证 | 人工抽查两个源码位置 |
| 15:30–20:30 | Prompt 05 现场生成 P0 API 自动化 | 计划→确认→编码→真实 HTTP 执行 | 失败不能通过改弱断言消除 |
| 20:30–25:00 | Prompt 06 现场生成 UI 核心流程 | 有头浏览器、动态用户、真实请求 | 查看生成的截图和 Trace |
| 25:00–28:00 | Prompt 07 核验证据和诊断 | XML、退出码、证据 SHA-256、缺陷分类 | 手动错误地址验证能变红 |
| 28:00–30:00 | Prompt 08 人工评审收口 | 追踪矩阵、未覆盖项、角色签字空位 | Agent 不能自行宣布验收通过 |

时间是上限，不追求现场生成全量测试。API 只完成 P0 纵向闭环和一个稳定边界；其余用例保留在设计和追踪矩阵中。

## 4. 现场逐步操作

### 阶段 A：需求到测试设计

1. 只打开 `docs/requirements/user-management.md`。
2. 输入 `prompts/01-requirement-analysis.md`。
3. 不展示 `presenter/product-clarifications.md`；讲师根据它扮演产品口头回复。
4. 要求 Agent 总结“已澄清/仍未决/冲突”，人工确认后才落地分析。
5. 输入 Prompt 02，先看候选 AC，再现场提出至少一条评审意见。
6. 输入明确口令：`AC 评审通过`。
7. 输入 Prompt 03，检查 AC—TC—执行层追踪和清理策略。
8. 输入明确口令：`测试设计评审通过`。

### 阶段 B：实现取证

1. 输入 Prompt 04，首次允许 Agent 读取 RuoYi-Vue 源码。
2. 要求 Agent 展开实际文件，不接受只报文件名。
3. 现场人工核对：
   - Java 用户名长度约束；
   - 用户详情查询是否过滤逻辑删除；
   - Vue 密码/手机号规则中的任一项。
4. 输入：`实现分析评审通过`，允许写入本轮结果。

### 阶段 C：现场生成 API 自动化

1. 输入 Prompt 05。
2. Agent 只能先输出计划；确认 TC/AC、文件和清理方式后输入：`API 计划确认`。
3. 观察其在 `demo-live/automation/api/` 创建代码。
4. 由讲师在终端执行 Agent 给出的命令，而不是让 Agent只复述结果。典型命令：

```bash
mvn -f demo-live/automation/api/pom.xml clean test-compile \
  2>&1 | tee demo-live/logs/api-compile.log

mvn -f demo-live/automation/api/pom.xml -Dtest='<P0测试类>' test \
  2>&1 | tee demo-live/logs/api-p0.log
api_status=${PIPESTATUS[0]}
printf '%s\n' "$api_status" | tee demo-live/logs/api-p0.exit-code
```

5. 查看原始 Surefire XML和持久化退出码：

```bash
cat demo-live/logs/api-p0.exit-code
find demo-live/automation/api/target/surefire-reports -type f -maxdepth 1 -print
```

6. 如果 21 位用户名测试失败，先对照 AC 和真实响应，禁止直接修改断言。

### 阶段 D：现场生成 UI 自动化及证据

1. 输入 Prompt 06；看完计划后输入：`UI 计划确认`。
2. 首次无头验证后，现场有头慢动作执行：

```bash
HEADLESS=false SLOW_MO=300 \
mvn -f demo-live/automation/ui/pom.xml test \
  2>&1 | tee demo-live/logs/ui.log
ui_status=${PIPESTATUS[0]}
printf '%s\n' "$ui_status" | tee demo-live/logs/ui.exit-code
```

3. 观众应亲眼看到登录、新增、搜索、删除。
4. 立即检查本轮生成的证据，而不是打开预置截图：

```bash
find demo-live/automation/ui/target -type f \
  \( -name '*.png' -o -name 'trace.zip' -o -name 'TEST-*.xml' \) -print
```

5. 打开两张本轮截图；使用 Trace Viewer 查看新增 POST 和删除 DELETE。
6. 核对 UI 自动报告中的文件大小与 SHA-256。

### 阶段 E：证明不是静态编造

先保留成功证据，再由讲师手动使用错误地址执行一次：

```bash
RUOYI_UI_URL=http://localhost:9999 HEADLESS=true \
mvn -f demo-live/automation/ui/pom.xml test \
  2>&1 | tee demo-live/logs/ui-negative-challenge.log
challenge_status=${PIPESTATUS[0]}
printf '%s\n' "$challenge_status" | tee demo-live/logs/ui-negative-challenge.exit-code
```

必须看到非零退出码和失败证据。随后恢复正确地址重跑。该步骤由讲师操作，不让 Agent 修改代码，证明测试能对真实系统变化作出反应。

### 阶段 F：结论

1. 输入 Prompt 07，让 Agent从 XML、Trace、截图和日志重新统计，不能照抄先前摘要。
2. 人工确认缺陷分类后输入：`执行结论确认`。
3. 输入 Prompt 08 做只读审查。
4. 最后明确：哪些动态通过、哪些失败、哪些只做了静态分析、哪些尚未覆盖。

## 5. 兜底策略（只在失败时启用）

### 5.1 启用规则

只有以下情况才切换对应阶段兜底：

- Agent 单阶段超过 3 分钟无有效输出；
- 网络/模型异常；
- 生成代码无法在 90 秒内修复；
- 浏览器或依赖出现非业务环境故障。

切换时必须当场说：

> “这一阶段现场生成受阻，现在切换到提前验证过的兜底材料；前面现场生成的需求、AC 和设计仍保留。”

不要整体切换，也不要把兜底结果冒充现场生成。

### 5.2 可审计的兜底命令

```bash
./scripts/use-demo-fallback.sh ac
./scripts/use-demo-fallback.sh cases
./scripts/use-demo-fallback.sh analysis
./scripts/use-demo-fallback.sh api
./scripts/use-demo-fallback.sh ui
./scripts/use-demo-fallback.sh reports
```

每次使用都会记录到：

```text
demo-live/FALLBACK_USED.md
```

复制自动化兜底时会排除 `.env` 和 `target/`，因此仍必须在现场重新执行，不会携带历史测试结果。

## 6. 现场检查清单

### 开场前

- [ ] Git 工作区干净，commit 已记录
- [ ] `demo-live` 已重置且没有预置产物
- [ ] 后端、Vue、Redis、MySQL可用
- [ ] 凭据仅在环境变量/.env 中
- [ ] Maven 和 Chromium依赖已缓存
- [ ] 兜底材料已验证但未打开

### 演示中

- [ ] Agent 首阶段只读原始需求
- [ ] 产品澄清由人提供
- [ ] AC、TC、计划均经过显式人工确认
- [ ] 实现结论有源码路径和方法证据
- [ ] 自动化写入 `demo-live/`，不是读取预置代码
- [ ] 测试由终端真实执行并展示退出码
- [ ] UI 使用本轮动态测试数据
- [ ] 本轮生成 Trace、截图和 Surefire XML
- [ ] 失败未通过弱化断言处理
- [ ] 任何兜底均有明确口头声明和文件记录

### 收口

- [ ] 从原始证据重新生成报告
- [ ] 动态验证与静态分析明确区分
- [ ] 检查数据清理和敏感信息
- [ ] 产品、开发、测试签字仍由人完成
