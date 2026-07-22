# RuoYi API 自动化

当前目录是独立于 RuoYi-Vue 业务源码的 Java 17、JUnit 5、RestAssured 和 Maven 黑盒 API 测试模块。

## 前置条件

- RuoYi-Vue 后端、MySQL 和 Redis 已启动。
- 仅连接可清理、可恢复的专用测试环境。
- 测试管理员拥有用户查询、新增和删除权限。
- 测试环境配置 `sys.account.captchaEnabled=false`。
- 不在命令、代码或日志中记录真实密码和完整 Token。

## 环境变量

| 变量 | 必需 | 说明 |
|---|---|---|
| `RUOYI_BASE_URL` | 是 | RuoYi-Vue 后端基础地址，例如 `http://localhost:8080`。 |
| `RUOYI_ADMIN_USERNAME` | 是 | 专用测试管理员账号。 |
| `RUOYI_ADMIN_PASSWORD` | 是 | 专用测试管理员密码。 |
| `RUOYI_ENABLE_SELF_DELETE_TEST` | 否 | 设置为 `true` 时执行高风险自删除保护测试。 |

测试不提供 base URL 或管理员凭据默认值。缺少必需变量时会在登录前明确失败。

## 执行 P0 测试

```bash
RUOYI_BASE_URL=http://localhost:8080 \
RUOYI_ADMIN_USERNAME='<test-admin>' \
RUOYI_ADMIN_PASSWORD='<test-password>' \
mvn -Dtest=UserManagementP0ApiTest test
```

P0 测试覆盖 TC-002、TC-003、TC-006、TC-007、TC-008、TC-009、TC-015 和 TC-016。

## 执行全部安全测试

```bash
RUOYI_BASE_URL=http://localhost:8080 \
RUOYI_ADMIN_USERNAME='<test-admin>' \
RUOYI_ADMIN_PASSWORD='<test-password>' \
mvn -Dtest='UserManagement*ApiTest' test
```

默认不执行 TC-014 自删除测试。其余需求差异测试不会跳过；如果当前业务实现不符合 AC，Maven 将真实报告失败。

## 执行自删除保护测试

自删除测试必须使用可恢复的专用管理员。若保护逻辑发生回归，该账号可能被逻辑删除。

```bash
RUOYI_BASE_URL=http://localhost:8080 \
RUOYI_ADMIN_USERNAME='<recoverable-test-admin>' \
RUOYI_ADMIN_PASSWORD='<test-password>' \
RUOYI_ENABLE_SELF_DELETE_TEST=true \
mvn -Dtest=UserManagementSelfDeleteApiTest test
```

## 数据与日志

- 每条测试动态生成用户名、手机号和测试用户密码。
- 每条测试独立登录，不依赖其他测试的执行结果或顺序。
- 所有可能成功创建用户的场景都会登记用户名，并在 `@AfterEach` 中尽力清理。
- 清理失败只输出用户名、HTTP 状态、业务码和业务信息，不输出管理员密码、测试用户密码、Authorization Header 或完整 Token。
- 删除后详情不可查询等已知差异仍按 AC 断言，不会为了获得绿色结果而降低标准。
