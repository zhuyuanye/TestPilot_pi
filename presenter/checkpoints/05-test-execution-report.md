# Checkpoint 05：测试执行报告

## 环境

- RuoYi-Vue 3.9.2
- API：`http://localhost:8080`
- Java 17 兼容测试工程
- JUnit 5 + RestAssured + Maven
- 验证码关闭

## 核心闭环结果

执行：

```bash
scripts/run-api-tests.sh
```

结果：

```text
UserLifecycleTest: Tests run: 2, Failures: 0, Errors: 0
UserRequirementGapTest: Skipped: 2
```

已验证：合法新增、列表查询、默认正常状态、精确重复用户名拒绝、删除后列表不可见、禁止删除当前用户。

## 需求差异结果

执行：

```bash
RUN_REQUIREMENT_GAPS=true scripts/run-api-tests.sh -Dtest=UserRequirementGapTest
```

结果：

```text
Tests run: 2, Failures: 2, Errors: 0
```

发现：

1. **用户名长度后端校验不符合需求**：后端接受 21 位用户名；对应 AC-03、TC-04。
2. **手机号后端校验不符合需求**：后端接受手机号 `123`；对应 AC-10、TC-13。

## 覆盖与风险

- 已自动化：AC-01、AC-02、AC-04、AC-11、AC-13。
- 已作为差异测试：AC-03、AC-10。
- 待补充：大小写唯一性、昵称、密码边界、删除后详情、删除不存在用户。
- UI 主流程尚未自动化；Vue 前端校验不能替代 API 校验。

## 测试结论

核心用户生命周期可执行，但当前实现不能完全满足已确认验收标准。至少存在两个后端参数校验差异，不建议将用户新增需求判定为全部通过。
