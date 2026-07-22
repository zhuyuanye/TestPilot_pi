# RuoYi API 自动化

独立于 RuoYi-Vue 源码的黑盒 API 验收测试，避免修改业务项目。

## 前置条件

- RuoYi-Vue 后端已启动
- MySQL、Redis 可用
- 测试管理员拥有用户查询、新增、删除权限
- `sys.account.captchaEnabled=false`
- 仅连接专用测试环境

## 执行核心闭环

```bash
cd automation/api
RUOYI_BASE_URL=http://localhost:8080 \
RUOYI_USERNAME=admin \
RUOYI_PASSWORD='你的测试密码' \
mvn test
```

默认覆盖：

- AC：正常新增、默认正常状态、重复用户名、删除、删除后列表不可见
- 权限：当前登录用户不能删除自己
- 安全约束：日志不打印密码和完整 Token
- 数据治理：动态生成唯一数据并在 `finally` 中清理

## 执行需求差异测试

以下测试预期在当前实现中暴露需求差异，因此默认跳过：

```bash
RUN_REQUIREMENT_GAPS=true \
RUOYI_USERNAME=admin \
RUOYI_PASSWORD='你的测试密码' \
mvn -Dtest=UserRequirementGapTest test
```

差异测试即使断言失败，也会在 `finally` 中清理创建的数据。
