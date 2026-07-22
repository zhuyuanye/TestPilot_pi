# RuoYi 用户管理自动化执行报告

## 1. 执行结论

| 项目 | 结果 |
|---|---|
| 测试代码编译 | 通过 |
| P0 API 测试 | 5 次执行，2 通过，3 失败 |
| 全量 API 测试 | 23 次执行，12 通过，10 失败，1 跳过 |
| 自删除保护测试 | 1 次执行，1 通过 |
| Vue UI 核心流程 | 1 次执行，1 通过 |
| 测试数据清理 | 未报告清理失败或可见残留 |
| 总体结论 | API 与 UI 自动化均可运行；当前业务实现不满足多项 AC |

测试使用 Java 17、JUnit 5、RestAssured 和 Maven，对运行中的 RuoYi-Vue 后端进行了真实 HTTP 调用。测试没有为获得绿色结果而放宽断言。

## 2. 环境诊断

最初 `.env` 中的 `RUOYI_BASE_URL` 指向 `http://localhost:8081`。该端口是 Vue 开发服务器：

- `http://localhost:8081/captchaImage` 对 RestAssured JSON 请求返回 HTTP 404。
- `http://localhost:8081/dev-api/captchaImage` 可通过前端代理访问后端。
- `http://localhost:8080/captchaImage` 可直接访问后端。

已仅将 `.env` 的 base URL 修正为 `http://localhost:8080`，未修改或输出账号密码。验证码接口返回 `captchaEnabled=false`，满足自动化登录前提。

## 3. 修改文件

| 文件 | 变更 |
|---|---|
| `automation/api/src/test/java/com/testpilot/ruoyi/RuoYiApiClient.java` | 重构环境配置、认证、请求超时、用户列表/详情/删除、业务断言和清理能力。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/TestData.java` | 重构动态用户名、手机号、密码、边界值和不存在用户 ID 生成。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/AbstractUserManagementApiTest.java` | 新增独立登录、前置清理、测试数据登记和失败后尽力清理。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/UserManagementP0ApiTest.java` | 新增 P0 生命周期、必填、唯一性、删除及数据一致性测试。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/UserManagementValidationApiTest.java` | 新增用户名、密码和手机号边界及等价类测试。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/UserManagementSelfDeleteApiTest.java` | 新增受显式环境开关保护的自删除测试。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/UserLifecycleTest.java` | 删除旧测试；其 TC/AC 编号与当前追踪关系不一致。 |
| `automation/api/src/test/java/com/testpilot/ruoyi/UserRequirementGapTest.java` | 删除默认跳过的差异测试；新测试直接报告需求差异。 |
| `automation/api/README.md` | 更新环境要求、执行命令、覆盖范围、清理及安全说明。 |
| `automation/api/.env.example` | 更新为确认后的环境变量名，不提供默认凭据。 |
| `automation/api/.env` | 仅修正 API base URL，由前端端口改为后端端口。 |
| `automation/ui/` | 新增 Playwright Java 的 TC-001 Vue 核心流程及 API 兜底清理。 |
| `scripts/run-all-tests.sh` | 新增环境检查、API 与 UI 的统一执行入口。 |
| `scripts/export-ui-result.py` | 将 UI Surefire XML 转换为可提交的 Markdown 执行结果。 |
| `outputs/ui-test-execution-report.md` | 持久化 UI 测试状态、明细、TC/AC 追踪与耗时。 |
| `outputs/defect-report.md` | 汇总动态验证缺陷和静态待确认风险。 |
| `outputs/review-signoff.md` | 增加 AC、TC 进入自动化前的人工评审门禁。 |
| `outputs/test-execution-report.md` | 更新为本次真实执行结果。 |

`automation/api/pom.xml` 已具备兼容的 Java 17、JUnit 5、RestAssured、Jackson 和 Maven Surefire 配置，本次未修改。

## 4. 执行命令和结果

### 4.1 干净编译

```bash
cd automation/api
mvn -DskipTests clean test
```

结果：`BUILD SUCCESS`

- 编译 6 个测试源文件。
- Java release 为 17。
- 仅验证依赖解析和测试代码编译。

### 4.2 P0 测试

```bash
cd automation/api
set -a && source .env && set +a
mvn -Dtest=UserManagementP0ApiTest test
```

结果：`BUILD FAILURE`

| 指标 | 数量 |
|---|---:|
| Tests run | 5 |
| Passed | 2 |
| Failures | 3 |
| Errors | 0 |
| Skipped | 0 |

### 4.3 全量 API 测试

```bash
cd automation/api
set -a && source .env && set +a
mvn -Dtest='UserManagement*ApiTest' test
```

结果：`BUILD FAILURE`

| 指标 | 数量 |
|---|---:|
| Tests run | 23 |
| Passed | 12 |
| Failures | 10 |
| Errors | 0 |
| Skipped | 1 |

全量命令中跳过项为高风险自删除测试 TC-014。Surefire 详细结果位于：

`automation/api/target/surefire-reports/`

### 4.4 自删除保护测试

```bash
RUOYI_ENABLE_SELF_DELETE_TEST=true \
./scripts/run-api-tests.sh -Dtest=UserManagementSelfDeleteApiTest
```

结果：`BUILD SUCCESS`，1 次执行，1 通过。当前用户删除请求被拒绝，随后仍可通过当前用户、列表和详情接口查询该管理员。

### 4.5 Vue UI 核心流程

```bash
HEADLESS=true ./scripts/run-ui-tests.sh
```

结果：`BUILD SUCCESS`，1 次执行，1 通过。通过真实 Vue 页面完成登录、新增、搜索确认、删除及列表消失验证，并通过 API 兜底清理。本次运行同时生成 Playwright `trace.zip`、新增查询截图、删除完成截图，并在独立 UI 报告中记录 SHA-256。

## 5. 通过结果

| TC / 数据行 | AC | 真实结果 |
|---|---|---|
| TC-002 | AC-01、AC-16、AC-19、AC-20 | 不填写手机号可以新增；列表可查询；未传状态时状态为“正常”。 |
| TC-003 | AC-02 | 空用户名被拒绝，返回失败状态及必填信息。 |
| TC-004 U2 | AC-03 | 2 位用户名新增成功。 |
| TC-004 U3 | AC-05 | 20 位用户名新增成功。 |
| TC-005 C1、C2 | AC-07 | 本次中文及符号用户名样本均新增成功。 |
| TC-006 | AC-08 | 完全相同用户名重复新增被拒绝。 |
| TC-007 | AC-08 | 仅大小写不同的用户名重复新增被拒绝；当前数据库环境表现为大小写不敏感。 |
| TC-010 P2 | AC-11 | 5 位密码新增成功。 |
| TC-010 P3 | AC-13 | 20 位密码新增成功。 |
| TC-011 S1、S2 | AC-15 | 仅字母或仅数字的 5 位密码均新增成功。 |
| TC-012 | AC-17 | 动态生成的合法 11 位手机号新增成功并正确返回。 |
| TC-013 M2 | AC-18 | 12 位手机号被拒绝。 |
| TC-015 部分 | AC-21、AC-22 | 管理员可以删除普通用户，删除后列表查询不到该用户。 |
| TC-016 | AC-25 | 删除不存在的数值用户 ID 后，基准用户信息和可查询性保持不变。 |

## 6. 失败结果和根因

| 失败项 | AC | 实际结果 | 结论 |
|---|---|---|---|
| TC-004 U1 | AC-04 | 1 位用户名返回业务成功并被创建。 | 服务端没有用户名最小 2 位校验。 |
| TC-004 U4 | AC-06 | 21 位用户名返回业务成功并被创建。 | 服务端用户名上限为 30，不符合 20 位上限。 |
| TC-008 | AC-09 | 空昵称被数据库约束拒绝，但响应暴露 SQL 异常且没有可理解地说明“昵称必填”。 | 缺少服务端参数校验，失败过晚且错误信息不符合产品澄清 8。 |
| TC-009 | AC-10 | 空密码返回业务成功并创建用户。 | 服务端没有密码必填校验。 |
| TC-010 P1 | AC-12 | 4 位密码返回业务成功并创建用户。 | 服务端没有密码最小 5 位校验。 |
| TC-010 P4 | AC-14 | 21 位密码返回业务成功并创建用户。 | 服务端没有密码最大 20 位校验。 |
| TC-013 M1 | AC-18 | 10 位手机号返回业务成功并创建用户。 | 服务端只校验最大 11 位，没有要求恰好 11 位。 |
| TC-013 M3 | AC-18 | 首位不是 1 的 11 位号码返回业务成功并创建用户。 | 服务端没有手机号首位校验。 |
| TC-013 M4 | AC-18 | 包含非数字字符的 11 位号码返回业务成功并创建用户。 | 服务端没有手机号数字格式校验。 |
| TC-015 详情 | AC-23 | 逻辑删除后，按原用户 ID 查询详情仍返回被删除用户。 | `selectUserById` 未过滤删除标记。 |

### TC-008 错误信息风险

空昵称响应包含数据库异常、Mapper 文件路径和 SQL 文本。这不仅不满足“可理解的信息”，还向调用方暴露内部实现细节，应优先由服务端参数校验提前拦截。

## 7. TC、AC 和自动化覆盖

| 测试范围 | TC | AC | 自动化实现 | 执行状态 |
|---|---|---|---|---|
| 有效新增、不填手机号、查询、默认状态、重复新增、删除、列表和详情不可查询 | TC-002、TC-006、TC-007、TC-015 | AC-01、AC-08、AC-16、AC-19～AC-23 | `UserManagementP0ApiTest.shouldCompleteP0UserLifecycle()` | 部分失败：AC-23 |
| 用户名必填 | TC-003 | AC-02 | `UserManagementP0ApiTest.shouldRejectMissingUserName()` | 通过 |
| 用户名长度边界 | TC-004 | AC-03～AC-06 | `UserManagementValidationApiTest.shouldEnforceUserNameLengthBoundaries()` | 2 通过、2 失败 |
| 用户名字符类型 | TC-005 | AC-07 | `UserManagementValidationApiTest.shouldAcceptSupportedUserNameCharacterTypes()` | 通过 |
| 昵称必填 | TC-008 | AC-09 | `UserManagementP0ApiTest.shouldRejectMissingNickName()` | 失败 |
| 密码必填 | TC-009 | AC-10 | `UserManagementP0ApiTest.shouldRejectMissingPassword()` | 失败 |
| 密码长度边界 | TC-010 | AC-11～AC-14 | `UserManagementValidationApiTest.shouldEnforcePasswordLengthBoundaries()` | 2 通过、2 失败 |
| 密码不要求复杂度 | TC-011 | AC-15 | `UserManagementValidationApiTest.shouldAcceptPasswordWithoutCharacterComplexity()` | 通过 |
| 合法手机号 | TC-012 | AC-17 | `UserManagementValidationApiTest.shouldAcceptValidPhoneNumber()` | 通过 |
| 非法手机号 | TC-013 | AC-18 | `UserManagementValidationApiTest.shouldRejectInvalidPhoneNumbers()` | 1 通过、3 失败 |
| 当前管理员不能删除自己 | TC-014 | AC-24 | `UserManagementSelfDeleteApiTest.shouldRejectDeletingCurrentUserAndKeepFixtureQueryable()` | 单独执行通过 |
| 删除不存在用户不影响其他用户 | TC-016 | AC-25 | `UserManagementP0ApiTest.shouldNotChangeOtherUsersWhenDeletingMissingUser()` | 通过 |
| Vue 用户生命周期 | TC-001 | AC-01、AC-19～AC-22 | `UserManagementUiTest.shouldCreateFindAndDeleteUserFromVuePage()` | 通过 |

## 8. 数据与日志安全

- 用户名、手机号和测试用户密码均为每次执行动态生成。
- 所有可能创建用户的场景均在 `@AfterEach` 中查询并删除残留。
- 本次执行未出现 `[cleanup]` 错误输出，未观察到列表可见残留。
- 测试没有输出管理员密码、测试用户密码、Authorization Header 或完整 Token。
- 失败诊断只引用业务状态、业务信息和非敏感测试标识。

## 9. 未覆盖风险

- TC-014 已在当前环境单独通过，但后续仍必须使用可恢复的专用管理员并显式设置 `RUOYI_ENABLE_SELF_DELETE_TEST=true`。
- TC-005 只覆盖了文档中的中文和符号代表值，不能穷举所有字符。
- 用户名大小写不敏感已在当前数据库环境通过，但应用层仍未显式保证，数据库排序规则变化可能导致回归。
- TC-001 只覆盖一个 UI 主流程，前端边界和异常提示仍以 API 测试及后续专项 UI 测试为主。
- 本次未直接检查数据库物理记录，符合验收范围。

## 10. 建议处理顺序

1. 增加服务端用户名 2～20、昵称必填、密码必填及 5～20、手机号格式校验。
2. 修复详情查询的逻辑删除过滤，满足 AC-23。
3. 将参数错误转化为稳定、可理解的业务失败信息，避免返回 SQL 和 Mapper 内部细节。
4. 明确用户名大小写唯一性的应用层或数据库层保证方式。
5. 保留 TC-014 的显式安全开关，并确保测试管理员具备恢复方案。
