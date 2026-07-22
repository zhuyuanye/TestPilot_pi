# RuoYi UI 自动化

使用 Playwright Java 验证 Vue 用户管理核心流程。

## 场景

```text
管理员登录 → 打开用户管理 → 新增用户
→ 搜索并确认用户 → 从页面删除用户 → 确认用户消失
```

测试完成后还会通过 API 兜底清理数据。

## 安装 Chromium

```bash
mvn -f automation/ui/pom.xml \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium" \
  -Dexec.classpathScope=test \
  exec:java
```

## 执行

```bash
RUOYI_ADMIN_USERNAME=admin \
RUOYI_ADMIN_PASSWORD='你的测试密码' \
HEADLESS=false \
SLOW_MO=300 \
./scripts/run-ui-tests.sh
```

现场演示建议使用有头模式和 200～400ms 的慢动作；日常执行使用 `HEADLESS=true`。
