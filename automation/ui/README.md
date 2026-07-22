# RuoYi UI 自动化

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

该 Markdown 报告包含执行状态、通过/失败数量、TC/AC 追踪、耗时和失败摘要，可以提交 Git。

## 失败证据

失败截图保存到：

`automation/ui/target/screenshots/TC-001-user-management-<timestamp>.png`

截图名称不包含用户名、密码或 Token。测试日志不打印请求体、Authorization Header 或完整 Token。
