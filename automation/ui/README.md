# RuoYi UI 自动化

> 演示说明：这是提前验证过的兜底工程。正常现场流程由 Prompt 06 在 `demo-live/automation/ui/` 从零生成；只有明确启用 UI 兜底后才复制本工程，并且必须现场重新生成 Trace 与截图。

使用 Java 17、JUnit 5、Playwright Java 和 Chromium 验证 Vue 用户管理核心流程。

## 覆盖场景

`TC-001 | AC-01、AC-19、AC-20、AC-21、AC-22`

```text
管理员登录 → 打开用户管理 → 新增用户
→ 搜索并确认用户及默认状态 → 页面删除用户 → 确认用户消失
```

测试完成后通过真实用户 API 查询并兜底清理同名测试数据。

## 环境变量

| 变量 | 必需 | 说明 |
|---|---|---|
| `RUOYI_UI_URL` | 是 | Vue 页面地址，例如 `http://localhost:8081`。 |
| `RUOYI_BASE_URL` | 是 | 后端 API 地址，例如 `http://localhost:8080`。 |
| `RUOYI_ADMIN_USERNAME` | 是 | 专用测试管理员账号。 |
| `RUOYI_ADMIN_PASSWORD` | 是 | 专用测试管理员密码。 |
| `HEADLESS` | 否 | 默认 `true`；设置为 `false` 时显示 Chromium。 |
| `SLOW_MO` | 否 | 默认 `0`；Playwright 操作间隔毫秒数。 |

可以将本地配置写入被 Git 忽略的 `automation/ui/.env`。脚本会自动加载该文件，也支持直接从当前终端环境读取变量。

## 安装或确认 Chromium

```bash
mvn -f automation/ui/pom.xml \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium" \
  -Dexec.classpathScope=test \
  exec:java
```

该命令只安装当前 Playwright 版本对应的 Chromium，不安装 Firefox 或 WebKit。

## 无头执行

```bash
HEADLESS=true SLOW_MO=0 ./scripts/run-ui-tests.sh
```

## 现场有头慢动作执行

```bash
HEADLESS=false SLOW_MO=300 ./scripts/run-ui-tests.sh
```

## 结果落地

每次通过 `scripts/run-ui-tests.sh` 执行后，Surefire XML 会自动转换并写入：

`outputs/ui-test-execution-report.md`

该 Markdown 报告包含执行状态、通过/失败数量、TC/AC 追踪、耗时、失败摘要，以及原始证据文件的 SHA-256，可以提交 Git。

## Trace 与截图证据

每次执行前会清空旧证据，并为本次运行创建唯一目录：

```text
automation/ui/target/evidence/TC-001-<timestamp>/
├── 01-user-created-and-found.png
├── 02-user-deleted.png
├── failure-<timestamp>.png       # 仅失败时生成
└── trace.zip
```

查看 Trace：

```bash
cd automation/ui
mvn -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="show-trace target/evidence/TC-001-<timestamp>/trace.zip" \
  -Dexec.classpathScope=test exec:java
```

证据目录位于 `target/`，默认不提交 Git。Trace 含页面快照、网络信息和输入操作，可能涉及测试账号信息，只能保存在本地或受控存储中。截图只在新增查询验证完成后、删除验证完成后及失败现场生成，登录页不会作为成功截图保存。测试日志不打印密码、Authorization Header 或完整 Token。
