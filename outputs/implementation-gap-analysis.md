# 用户管理实现差异分析

## 1. 分析范围

本报告基于以下材料进行静态分析：

- 需求：`docs/requirements/user-management.md`
- 验收标准：`outputs/user-management-acceptance-criteria.md`
- 测试用例：`outputs/user-management-test-cases.md`
- 业务项目：`/Users/zhuyuanye/Documents/Code/RuoYi-Vue`

本阶段未修改 RuoYi-Vue 业务代码，未生成测试代码，也未运行接口或数据库验证。以下实现结论均来自代码文件、类和方法；数据库排序规则导致的行为明确标记为环境依赖。

## 2. 总体结论

| 领域 | 结论 | 主要代码证据 |
|---|---|---|
| Vue 用户管理 | 页面具备列表、新增、详情和删除流程；用户名 2～20、昵称必填和默认正常状态在前端实现，但密码与手机号规则比 AC 更严格或存在正则问题。 | `ruoyi-ui/src/views/system/user/index.vue`：`getList()`、`handleAdd()`、`handleDelete()`、`rules`、`reset()`；`ruoyi-ui/src/utils/passwordRule.js`：`pwdValidator` |
| Java 用户管理 | Controller、Service、Mapper 和逻辑删除链路完整；新增接口的服务端字段校验与 AC 存在明显差异。 | `SysUserController.add()`、`remove()`；`SysUserServiceImpl.insertUser()`、`deleteUserByIds()`；`SysUserMapper.xml` |
| 用户名唯一性 | 代码只使用数据库等值比较，没有显式忽略大小写；是否满足 AC-08 取决于数据库排序规则。 | `SysUserServiceImpl.checkUserNameUnique()`；`SysUserMapper.xml` 的 `checkUserNameUnique` |
| 删除一致性 | 列表查询会过滤已删除用户，但按 ID 查询详情不检查删除标记，违反 AC-23。 | `SysUserMapper.xml` 的 `selectUserList`、`selectUserById`、`deleteUserByIds` |
| 自删除保护 | Controller 明确拒绝删除当前登录用户，满足 AC-24。 | `SysUserController.remove()` |
| Token | 登录返回 Token；前端保存在 `Admin-Token` Cookie，并通过 `Authorization: Bearer <token>` 传递。 | `SysLoginController.login()`；`ruoyi-ui/src/utils/auth.js`；`ruoyi-ui/src/utils/request.js` 请求拦截器 |
| 测试数据 | 现有用户接口可完成准备和清理，但删除必须使用数值用户 ID，手机号还有需求未规定的唯一性约束。 | `SysUserController.list()`、`add()`、`remove()`；`SysUserServiceImpl.checkPhoneUnique()` |
| 测试基础设施 | 项目没有后端或前端自动化测试框架、测试目录、测试脚本和有效 CI 测试流水线。 | 根及子模块 `pom.xml`；`ruoyi-ui/package.json`；各模块目录；`.github/workflows` |

## 3. 需求或 AC 明确规定

| 规则 | 明确要求 | 来源 |
|---|---|---|
| 用户名 | 必填、2～20 个字符、唯一、不区分大小写，不限制字符类型。 | 原始需求 §2；产品澄清 1、2；AC-02～AC-08 |
| 昵称 | 必填。 | 原始需求 §2；AC-09 |
| 密码 | 必填、5～20 个字符，不要求字符复杂度。 | 原始需求 §2；产品澄清 4；AC-10～AC-15 |
| 手机号 | 选填；填写时必须是 11 位数字且第一位为 1。 | 原始需求 §2；产品澄清 3；AC-16～AC-18 |
| 新增结果 | 新增成功后可从用户列表查询，默认状态为“正常”；不要求验证立即登录。 | 原始需求 §2；产品澄清 5；AC-19、AC-20 |
| 删除权限 | 管理员可以删除普通系统用户，当前登录用户不能删除自己。 | 原始需求 §3；AC-21、AC-24 |
| 删除结果 | 删除后列表和详情均不能查询到用户；不直接验证数据库物理记录。 | 原始需求 §3；产品澄清 6、7；AC-22、AC-23 |
| 不存在用户 | 删除不存在的用户不能影响其他用户。 | 原始需求 §3；AC-25 |
| 失败结果 | 必须返回失败状态和能够说明失败原因的信息，文案不要求逐字匹配。 | 产品澄清 8 |

## 4. 当前代码实际实现

### 4.1 Vue 用户管理页面、表单校验和 API 封装

| 实现点 | 当前实现 | 代码证据 |
|---|---|---|
| 用户管理页面 | `User` 组件负责用户列表、查询、新增、修改和删除；`getList()` 调用 `listUser()`，`handleAdd()` 初始化新增表单，`handleDelete()` 删除后重新调用 `getList()`。 | `ruoyi-ui/src/views/system/user/index.vue`：组件 `User`、`getList()`、`handleAdd()`、`handleDelete()` |
| 用户详情 | 点击用户名后由 `handleViewData()` 打开 `UserViewDrawer`，详情组件调用 `getUser(userId)`。 | `ruoyi-ui/src/views/system/user/index.vue`：`handleViewData()`；`ruoyi-ui/src/views/system/user/view.vue`：组件 `UserViewDrawer` |
| 用户名校验 | 前端规则要求必填且长度为 2～20，没有用户名字符类型正则。 | `ruoyi-ui/src/views/system/user/index.vue`：`rules.userName` |
| 昵称校验 | 前端规则要求昵称必填。 | `ruoyi-ui/src/views/system/user/index.vue`：`rules.nickName` |
| 密码校验 | 新增表单通过 `pwdValidator` 校验密码，要求 6～20 个字符；字符限制由 `pwdChrtype` 决定，默认规则还禁止部分特殊字符。 | `ruoyi-ui/src/views/system/user/index.vue`：密码表单项；`ruoyi-ui/src/utils/passwordRule.js`：`pwdValidator`、`pwdPromptValidator` |
| 手机号校验 | 手机号选填；前端使用 `/^1[3|4|5|6|7|8|9][0-9]\d{8}$/`。该表达式要求 11 个字符并通常限定第二位为 3～9，但字符类中的 `|` 也会被当作可接受字符。 | `ruoyi-ui/src/views/system/user/index.vue`：`rules.phonenumber` |
| 默认状态 | `reset()` 将新增表单的 `status` 初始化为字符串 `"0"`。 | `ruoyi-ui/src/views/system/user/index.vue`：`reset()` |
| 超级管理员操作入口 | 用户 ID 为 1 时，行操作区域不显示。该条件针对固定超级管理员，不是通用的“当前登录用户”。 | `ruoyi-ui/src/views/system/user/index.vue`：操作列 `v-if="scope.row.userId !== 1"` |
| 用户 API | 封装了 `listUser()`、`getUser()`、`addUser()`、`updateUser()`、`delUser()` 等方法。 | `ruoyi-ui/src/api/system/user.js`：对应导出函数 |

用户 API 封装如下：

| 前端函数 | HTTP 方法 | 请求地址 | 代码证据 |
|---|---|---|---|
| `listUser(query)` | GET | `/system/user/list` | `ruoyi-ui/src/api/system/user.js`：`listUser()` |
| `getUser(userId)` | GET | `/system/user/{userId}`；无 ID 时为 `/system/user/` | `ruoyi-ui/src/api/system/user.js`：`getUser()` |
| `addUser(data)` | POST | `/system/user` | `ruoyi-ui/src/api/system/user.js`：`addUser()` |
| `updateUser(data)` | PUT | `/system/user` | `ruoyi-ui/src/api/system/user.js`：`updateUser()` |
| `delUser(userId)` | DELETE | `/system/user/{userIds}` | `ruoyi-ui/src/api/system/user.js`：`delUser()` |

### 4.2 Java Controller、Service、领域对象和参数校验

| 层级 | 类或文件 | 关键方法及职责 |
|---|---|---|
| Controller | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java` | `list()`、`getInfo()`、`add()`、`update()`、`remove()`；负责权限校验、参数校验、唯一性检查和调用用户服务。 |
| Service 接口 | `ruoyi-system/src/main/java/com/ruoyi/system/service/ISysUserService.java` | `selectUserList()`、`selectUserById()`、`insertUser()`、`deleteUserById()`、`deleteUserByIds()`、`checkUserNameUnique()`。 |
| Service 实现 | `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java` | `checkUserNameUnique()`、`checkPhoneUnique()`、`checkUserAllowed()`、`insertUser()`、`deleteUserByIds()`。 |
| 领域对象 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java` | 用户字段及 Bean Validation 注解。 |
| Mapper 接口 | `ruoyi-system/src/main/java/com/ruoyi/system/mapper/SysUserMapper.java` | 用户查询、新增、更新和删除的数据访问方法。 |
| Mapper SQL | `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml` | `selectUserList`、`selectUserById`、`checkUserNameUnique`、`insertUser`、`deleteUserByIds` 等 SQL。 |

`SysUser` 当前新增参数校验：

| 字段 | Java 校验 | 代码证据 |
|---|---|---|
| `userName` | `@NotBlank`、`@Size(min=0, max=30)`、`@Xss`；没有最小 2 或最大 20 的服务端约束。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java`：`getUserName()` |
| `nickName` | `@Size(min=0, max=30)`、`@Xss`；没有 `@NotBlank`。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java`：`getNickName()` |
| `password` | `SysUser` 上没有 `@NotBlank` 或 `@Size`。`SysUserController.add()` 直接调用 `SecurityUtils.encryptPassword(user.getPassword())`。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java`：`getPassword()`；`ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java`：`add()` |
| `phonenumber` | 仅有 `@Size(min=0, max=11)`，没有数字或首位为 1 的格式校验。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java`：`getPhonenumber()` |
| `email` | `@Email`、`@Size(min=0, max=50)`。该规则不在本次需求中。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java`：`getEmail()` |

`SysUserController.add()` 使用 `@Validated`，并在插入前调用：

- `deptService.checkDeptDataScope(user.getDeptId())`
- `roleService.checkRoleDataScope(user.getRoleIds())`
- `userService.checkUserNameUnique(user)`
- `userService.checkPhoneUnique(user)`
- `userService.checkEmailUnique(user)`
- `SecurityUtils.encryptPassword(user.getPassword())`
- `userService.insertUser(user)`

代码证据：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java`：`add()`。

### 4.3 登录、新增、查询、详情和删除接口

| 能力 | 接口 | 权限或认证 | Controller 方法 | 后续实现 |
|---|---|---|---|---|
| 登录 | `POST /login` | 匿名访问；登录过程可要求验证码 | `SysLoginController.login()` | `SysLoginService.login()` → Spring Security 认证 → `TokenService.createToken()` |
| 当前用户信息 | `GET /getInfo` | 已认证 | `SysLoginController.getInfo()` | 返回当前用户、角色和权限 |
| 用户列表 | `GET /system/user/list` | `system:user:list` | `SysUserController.list()` | `SysUserServiceImpl.selectUserList()` → `SysUserMapper.xml` 的 `selectUserList` |
| 用户详情或新增元数据 | `GET /system/user/{userId}` 或 `GET /system/user/` | `system:user:query` | `SysUserController.getInfo()` | `SysUserServiceImpl.selectUserById()`；无 ID 时主要返回角色和岗位选项 |
| 新增用户 | `POST /system/user` | `system:user:add` | `SysUserController.add()` | 唯一性检查、密码 BCrypt 加密、`SysUserServiceImpl.insertUser()` |
| 删除用户 | `DELETE /system/user/{userIds}` | `system:user:remove` | `SysUserController.remove()` | 自删除检查后调用 `SysUserServiceImpl.deleteUserByIds()` |

代码路径：

- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java`：`login()`、`getInfo()`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java`：`list()`、`getInfo()`、`add()`、`remove()`
- `ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java`：`login()`、`validateCaptcha()`、`loginPreCheck()`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java`：`selectUserList()`、`selectUserById()`、`insertUser()`、`deleteUserByIds()`

列表 SQL `selectUserList` 固定包含 `u.del_flag = '0'`，并支持用户名模糊查询、手机号模糊查询、状态、部门和数据权限过滤。代码证据：`ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`selectUserList`。

详情 SQL `selectUserById` 没有 `del_flag = '0'` 条件。代码证据：`ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`selectUserById`。

### 4.4 Token 获取和传递

| 阶段 | 当前实现 | 代码证据 |
|---|---|---|
| 登录请求 | `login.vue` 的 `handleLogin()` 派发 Vuex `Login`；`login()` API 发送用户名、密码、验证码和 UUID。 | `ruoyi-ui/src/views/login.vue`：`handleLogin()`；`ruoyi-ui/src/store/modules/user.js`：`Login`；`ruoyi-ui/src/api/login.js`：`login()` |
| Token 返回 | `POST /login` 返回 `token`。 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java`：`login()` |
| Token 存储 | 前端以 Cookie 保存，键名为 `Admin-Token`。 | `ruoyi-ui/src/utils/auth.js`：`TokenKey`、`getToken()`、`setToken()`、`removeToken()` |
| Token 传递 | Axios 请求拦截器默认读取 Cookie，并设置 `Authorization: Bearer <token>`；`headers.isToken === false` 时跳过。 | `ruoyi-ui/src/utils/request.js`：请求拦截器 |
| 后端解析 | JWT 过滤器读取 Authorization Bearer Token，通过 `TokenService` 校验 Redis 中的登录会话并设置安全上下文。 | `ruoyi-framework/src/main/java/com/ruoyi/framework/security/filter/JwtAuthenticationTokenFilter.java`：`doFilterInternal()`；`ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/TokenService.java`：Token 读取、校验和刷新方法 |
| 退出登录 | Vuex `LogOut` 调用退出接口并清除 Token、角色和权限。 | `ruoyi-ui/src/store/modules/user.js`：`LogOut`；`ruoyi-ui/src/api/login.js`：`logout()` |

API 自动化获取 Token 的实现前提：

1. 调用 `GET /captchaImage` 获取验证码开关、UUID 和图片。
2. 验证码启用时，`SysLoginService.validateCaptcha()` 会从 Redis 的验证码键读取并校验输入。
3. 调用 `POST /login`，提交 `LoginBody` 中的 `username`、`password`、`code`、`uuid`。
4. 从成功响应读取 `token`，后续请求发送 `Authorization: Bearer <token>`。

代码证据：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/common/CaptchaController.java`：`getCode()`；`ruoyi-common/src/main/java/com/ruoyi/common/core/domain/model/LoginBody.java`；`ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java`：`validateCaptcha()`；`ruoyi-ui/src/api/login.js`：`getCodeImg()`、`login()`。

### 4.5 用户名唯一性和大小写处理

`SysUserController.add()` 调用 `SysUserServiceImpl.checkUserNameUnique()`。该方法通过 Mapper 查询同名用户，并在新增时把任何查询结果判定为冲突。

Mapper SQL 为：按 `user_name = #{userName}` 等值匹配、限定 `del_flag = '0'`、取第一条记录。代码证据：

- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java`：`add()`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java`：`checkUserNameUnique()`
- `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`checkUserNameUnique`

代码中没有 `lower()`、`upper()`、`equalsIgnoreCase()`、`toLowerCase()` 或显式大小写不敏感排序规则。因此：

- 应用代码本身不能保证用户名唯一性不区分大小写。
- 实际结果由 MySQL 数据库或字段使用的排序规则决定。
- 初始化 SQL 没有为 `sys_user.user_name` 显式指定大小写规则。代码证据：`sql/ry_20260417.sql` 中 `sys_user` 表定义。
- 唯一性查询只检查 `del_flag = '0'` 的用户，因此已逻辑删除用户名可再次使用。代码证据：`SysUserMapper.xml` 的 `checkUserNameUnique`。

### 4.6 手机号、密码和用户名长度校验

| 规则 | 需求或 AC | Vue 实现 | Java 实现 | 结论 |
|---|---|---|---|---|
| 用户名必填 | 必填 | `rules.userName.required` | `SysUser.getUserName()` 的 `@NotBlank` | 前后端一致。 |
| 用户名长度 | 2～20 | `rules.userName` 为 2～20 | `SysUser.getUserName()` 为 0～30；`UserConstants` 的 2～20 只由 `SysLoginService.loginPreCheck()` 用于登录 | 新增 API 不满足 AC-03～AC-06。 |
| 用户名字符 | 不限制类型 | 前端无字符正则 | `SysUser.getUserName()` 有 `@Xss` | 后端对可疑 HTML/XSS 内容存在额外限制。 |
| 昵称必填 | 必填 | `rules.nickName.required` | `SysUser.getNickName()` 没有 `@NotBlank` | 仅 UI 保证，API 可绕过。 |
| 密码必填 | 必填 | `pwdValidator.required` | `SysUser.getPassword()` 没有必填注解 | 仅 UI 有明确校验。 |
| 密码长度 | 5～20 | `pwdValidator` 为 6～20 | 新增用户领域对象没有密码长度注解；5～20 常量只用于登录和注册预检查 | UI 拒绝 5 位密码，API 不保证拒绝 4 位或 21 位密码。 |
| 密码复杂度 | 不要求 | `passwordRule.js` 根据 `pwdChrtype` 校验；默认也禁止部分字符 | 新增用户没有对应 Bean Validation | UI 存在需求未规定的字符限制。 |
| 手机号选填 | 选填 | 没有 required 规则 | 没有必填注解 | 一致。 |
| 手机号格式 | 11 位数字、首位 1 | 前端正则更严格且字符类写法允许 `|` | `SysUser.getPhonenumber()` 只限制最多 11 位 | UI 和 API 均不能完整、准确地保证 AC-17、AC-18。 |
| 手机号唯一性 | 需求未规定 | 前端无对应说明 | `SysUserController.add()` 调用 `checkPhoneUnique()` | 实现额外增加业务限制。 |

相关代码：

- `ruoyi-ui/src/views/system/user/index.vue`：`rules`
- `ruoyi-ui/src/utils/passwordRule.js`：`pwdValidator`、`pwdPromptValidator`
- `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java`：字段 getter 校验注解
- `ruoyi-common/src/main/java/com/ruoyi/common/constant/UserConstants.java`：用户名和密码长度常量
- `ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/SysLoginService.java`：`loginPreCheck()`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java`：`add()`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java`：`checkPhoneUnique()`

### 4.7 禁止删除当前登录用户

`SysUserController.remove()` 在调用 Service 前检查待删除 ID 数组是否包含 `getUserId()`。包含时返回错误信息“当前用户不能删除”，不执行删除。

代码证据：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java`：`remove()`。

Service 还执行两项保护：

- `SysUserServiceImpl.checkUserAllowed()` 禁止操作用户 ID 为 1 的超级管理员。
- `SysUserServiceImpl.checkUserDataScope()` 校验当前操作者的数据权限。

代码证据：`ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java`：`checkUserAllowed()`、`checkUserDataScope()`、`deleteUserByIds()`。

前端只对用户 ID 为 1 隐藏行操作。如果当前登录管理员不是 ID 1，其自删除入口仍可能显示，但后端会拒绝。代码证据：`ruoyi-ui/src/views/system/user/index.vue`：操作列条件；`SysUserController.remove()`。

### 4.8 删除方式和查询一致性

删除为逻辑删除：`deleteUserById` 和 `deleteUserByIds` 执行 `UPDATE sys_user SET del_flag = '2'`。批量删除时还清理用户角色和用户岗位关联。

代码证据：

- `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`deleteUserById`、`deleteUserByIds`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java`：`deleteUserByIds()`

查询行为：

| 查询 | 删除后行为 | 代码证据 |
|---|---|---|
| 用户列表 | `selectUserList` 有 `u.del_flag = '0'`，删除用户不可见。 | `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`selectUserList` |
| 按用户名查询 | `selectUserByUserName` 有 `del_flag = '0'`，删除用户不可见。 | `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`selectUserByUserName` |
| 唯一性查询 | `checkUserNameUnique` 有 `del_flag = '0'`，删除用户名可复用。 | `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`checkUserNameUnique` |
| 按 ID 查询详情 | `selectUserById` 没有删除标记条件，删除用户仍可能被查到。 | `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`：`selectUserById` |

### 4.9 测试数据准备和清理能力

| 测试需要 | 可用能力 | 实现证据 | 限制或注意事项 |
|---|---|---|---|
| 获取管理员 Token | 验证码接口、登录接口、Bearer Token | `CaptchaController.getCode()`；`SysLoginController.login()`；`SysLoginService.login()`；`TokenService.createToken()` | 验证码启用时，自动化需要可控验证码配置或测试专用获取方式；项目当前没有测试 Profile 或测试辅助方法。 |
| 新增测试用户 | `POST /system/user` | `SysUserController.add()` | Token 必须拥有 `system:user:add`；还会执行部门、角色、用户名、手机号和邮箱检查。 |
| 按用户名定位用户 | `GET /system/user/list` 支持 `userName` 模糊过滤 | `SysUserController.list()`；`SysUserMapper.xml`：`selectUserList` | 返回分页结果并受数据权限约束，清理前应精确匹配返回用户名。 |
| 获取用户 ID | 列表响应或详情响应包含用户 ID | `TableDataInfo`；`SysUserController.list()`、`getInfo()` | 删除路径接收 `Long[]`，测试必须先解析数值用户 ID。 |
| 删除单个或多个测试用户 | `DELETE /system/user/{userIds}` | `SysUserController.remove()`；`SysUserServiceImpl.deleteUserByIds()` | 是逻辑删除；不能删除当前用户或 ID 1 用户。 |
| 验证列表清理 | 删除后列表过滤 `del_flag = '0'` | `SysUserMapper.xml`：`selectUserList` | 可以验证 AC-22。 |
| 验证详情清理 | 详情按 ID 查询 | `SysUserMapper.xml`：`selectUserById` | 当前实现不会过滤逻辑删除用户，无法满足 AC-23。 |
| 重复使用用户名 | 唯一性查询忽略逻辑删除用户 | `SysUserMapper.xml`：`checkUserNameUnique` | 同名测试数据可重新创建，但数据库会累积逻辑删除记录。 |
| 重复使用手机号 | 手机号唯一性只检查未删除用户 | `SysUserServiceImpl.checkPhoneUnique()`；`SysUserMapper.xml`：`checkPhoneUnique` | 并行测试或清理失败时，固定手机号会产生冲突。 |

对现有测试用例数据策略的直接影响：

1. `TC-016` 的不存在目标 `missing-{R6}-16` 不能直接作为删除参数，因为 `SysUserController.remove()` 的路径变量类型为 `Long[]`。应在实现自动化时使用确认不存在的数值型用户 ID。
2. `TC-001` 和 `TC-012` 复用固定手机号 `13800000000`。由于 `SysUserController.add()` 调用 `checkPhoneUnique()`，并行执行或前序清理失败时可能发生需求之外的手机号冲突。
3. `TC-004` 的固定用户名 `q2` 可以在逻辑删除后重新创建，因为 `checkUserNameUnique` 忽略 `del_flag = '2'`；但会持续累积逻辑删除记录。
4. 删除清理需要先通过列表结果取得数值型 `userId`，再调用删除接口；只记录用户名不足以直接删除。

### 4.10 项目现有测试框架和可复用工具

#### 当前测试框架

| 范围 | 当前状态 | 代码证据 |
|---|---|---|
| 后端测试框架 | 根 POM 和子模块没有测试依赖；没有 `spring-boot-starter-test`、JUnit、Mockito、AssertJ、Surefire 或 Failsafe 配置。 | `pom.xml` 及各模块 `pom.xml` |
| 后端测试目录 | 各模块没有 `src/test/java` 或 `src/test/resources`。 | RuoYi-Vue 各 Maven 模块目录 |
| 前端测试框架 | 没有 Jest、Vitest、Mocha、Cypress、Playwright 或 Vue Test Utils 依赖，也没有测试脚本。 | `ruoyi-ui/package.json` |
| 前端测试文件 | 没有 `__tests__`、`*.spec.js`、`*.test.js` 或对应 TypeScript 测试文件。 | `ruoyi-ui` 目录 |
| CI 测试 | 没有执行自动化测试的有效流水线配置。 | `.github/workflows`；仓库根 `AGENTS.md` 的项目说明 |
| `TestController` | 这是 Swagger 演示用的内存 CRUD Controller，不验证真实 `sys_user` 业务，不可替代用户管理测试。 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/tool/TestController.java`：列表、新增、修改和删除演示方法 |

#### 可复用运行时能力

| 工具 | 可复用内容 | 代码证据 |
|---|---|---|
| `AjaxResult` | 标准响应结构，包含 `code`、`msg` 和数据。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/AjaxResult.java` |
| `TableDataInfo` | 用户列表分页响应，包含 `total`、`rows`、`code`、`msg`。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/page/TableDataInfo.java` |
| `BaseController` | `startPage()`、`getDataTable()`、`toAjax()`、`success()`、`error()`、当前用户获取方法。 | `ruoyi-common/src/main/java/com/ruoyi/common/core/controller/BaseController.java` |
| `SecurityUtils` | BCrypt 密码加密和匹配、管理员 ID 判断。 | `ruoyi-common/src/main/java/com/ruoyi/common/utils/SecurityUtils.java`：`encryptPassword()`、`matchesPassword()`、`isAdmin()` |
| `TokenService` | Token 创建、解析、Redis 会话验证和刷新。 | `ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/TokenService.java` |
| `ISysUserService` | 用户查询、新增、唯一性检查和删除能力，可供未来后端集成测试使用。 | `ruoyi-system/src/main/java/com/ruoyi/system/service/ISysUserService.java` |
| OpenAPI | 项目启用 Springdoc，可用于确认接口契约。 | `ruoyi-admin/src/main/resources/application.yml`：Springdoc 配置 |
| 初始化 SQL | 提供表结构、管理员、普通用户、角色和权限种子数据。 | `sql/ry_20260417.sql` |

环境和基础地址相关代码：

- 后端端口和上下文：`ruoyi-admin/src/main/resources/application.yml`
- 数据库连接：`ruoyi-admin/src/main/resources/application-druid.yml`
- 前端 Axios 基础地址：`ruoyi-ui/src/utils/request.js`
- 开发环境 API 前缀：`ruoyi-ui/.env.development`
- 前端代理目标：`ruoyi-ui/vue.config.js`

## 5. 差异、风险或待确认项

| 编号 | 严重度 | 差异、风险或待确认项 | 影响 AC / TC | 代码证据 |
|---|---|---|---|---|
| GAP-01 | 高 | 新增 API 的用户名长度允许 1～30 范围内的非空值，而 AC 要求 2～20。UI 可阻止，但 API 自动化会暴露服务端差异。 | AC-03～AC-06；TC-004 | `SysUser.getUserName()`；`SysUserController.add()` |
| GAP-02 | 高 | 昵称必填只在 Vue 实现，服务端没有 `@NotBlank`。直接调用新增 API 可能接受空昵称。 | AC-09；TC-008 | `index.vue` 的 `rules.nickName`；`SysUser.getNickName()` |
| GAP-03 | 高 | 密码规则三方不一致：AC 为 5～20 且无复杂度；Vue 为 6～20 并有字符限制；新增 API 没有密码必填或长度注解。 | AC-10～AC-15；TC-009～TC-011 | `passwordRule.js` 的 `pwdValidator`；`SysUser.getPassword()`；`SysUserController.add()` |
| GAP-04 | 高 | 手机号规则不一致：Vue 比需求更严格且正则字符类允许 `|`；Java 只限制最大 11 位，不能拒绝 10 位、非数字或不以 1 开头的数据。 | AC-17、AC-18；TC-012、TC-013 | `index.vue` 的 `rules.phonenumber`；`SysUser.getPhonenumber()` |
| GAP-05 | 高 | 用户名唯一性没有显式忽略大小写，结果依赖数据库排序规则，无法由应用代码保证 AC-08。 | AC-08；TC-007 | `SysUserServiceImpl.checkUserNameUnique()`；`SysUserMapper.xml` 的 `checkUserNameUnique`；`ry_20260417.sql` |
| GAP-06 | 高 | 删除后详情仍可能按 ID 查询到用户，因为 `selectUserById` 没有过滤 `del_flag`。 | AC-23；TC-015 | `SysUserMapper.xml` 的 `selectUserById`、`deleteUserByIds` |
| GAP-07 | 中 | 代码额外要求手机号唯一，需求和 AC 未规定。合法手机号可能因与现有用户重复而新增失败。 | AC-01、AC-17；TC-001、TC-012 | `SysUserController.add()`；`SysUserServiceImpl.checkPhoneUnique()`；`SysUserMapper.xml` 的 `checkPhoneUnique` |
| GAP-08 | 中 | 用户名声明为不限制字符类型，但后端 `@Xss` 会拒绝部分 HTML 或脚本形式输入。需要确认安全过滤是否作为全局非功能要求优先于该澄清。 | AC-07；TC-005 | `SysUser.getUserName()` 的 `@Xss` |
| GAP-09 | 中 | 已删除用户名可重新使用，因为唯一性查询只检查未删除用户。此前该业务边界被排除在 AC 外，仍应记录为实现行为。 | 当前 AC 未覆盖 | `SysUserMapper.xml` 的 `checkUserNameUnique` |
| GAP-10 | 中 | 前端只隐藏 ID 1 用户的删除入口，不隐藏任意当前登录用户的删除入口；非 ID 1 管理员可能在 UI 发起自删除后才收到后端拒绝。 | AC-24；TC-001、TC-014 | `index.vue` 操作列条件；`SysUserController.remove()` |
| GAP-11 | 中 | API 自动化登录可能受图片验证码阻塞；项目没有测试 Profile 或现成测试辅助接口来稳定获取验证码答案。 | 所有 API 自动化 TC | `CaptchaController.getCode()`；`SysLoginService.validateCaptcha()`；`application.yml` |
| GAP-12 | 中 | 测试设计中的不存在目标是字符串，但删除接口参数为 `Long[]`，无法直接用于 TC-016 自动化。 | TC-016 | `SysUserController.remove()` |
| GAP-13 | 中 | 固定手机号会受到实现中的手机号唯一性影响，测试并行执行或清理失败时会出现非目标失败。 | TC-001、TC-012 | `SysUserServiceImpl.checkPhoneUnique()`；`SysUserMapper.xml` 的 `checkPhoneUnique` |
| GAP-14 | 中 | 项目没有现有自动化测试框架、测试脚本、隔离数据库/Redis、测试 Profile 或 CI 测试任务，后续自动化不能直接复用现成测试骨架。 | 全部自动化 TC | 根 `pom.xml`、各模块 `pom.xml`、`ruoyi-ui/package.json`、各模块目录、`.github/workflows` |
| GAP-15 | 低 | 重复创建并逻辑删除固定用户名会在数据库持续累积删除记录；虽然不影响当前唯一性检查，但可能影响长期测试环境维护。 | TC-004 及重复执行清理 | `SysUserMapper.xml` 的 `deleteUserByIds`、`checkUserNameUnique` |

## 6. AC 实现状态汇总

| AC 范围 | 静态分析状态 | 说明 | 代码证据 |
|---|---|---|---|
| AC-01 | 部分满足 | 核心新增链路存在，但服务端校验和额外手机号唯一性可能改变有效数据结果。 | `SysUserController.add()`；`SysUserServiceImpl.insertUser()`、`checkPhoneUnique()` |
| AC-02 | 满足 | 前端 required 和服务端 `@NotBlank` 均覆盖用户名必填。 | `index.vue` 的 `rules.userName`；`SysUser.getUserName()` |
| AC-03～AC-06 | 部分满足 | Vue 为 2～20；新增 API 为非空且最大 30，没有最小 2。 | `index.vue` 的 `rules.userName`；`SysUser.getUserName()`；`SysUserController.add()` |
| AC-07 | 部分满足 | Vue 无字符类型限制；后端 `@Xss` 形成额外限制。 | `index.vue` 的 `rules.userName`；`SysUser.getUserName()` |
| AC-08 | 待环境确认 | 是否不区分大小写取决于数据库排序规则。 | `SysUserServiceImpl.checkUserNameUnique()`；`SysUserMapper.xml` 的 `checkUserNameUnique`；`ry_20260417.sql` |
| AC-09 | 部分满足 | Vue 必填，Java 新增参数未强制必填。 | `index.vue` 的 `rules.nickName`；`SysUser.getNickName()` |
| AC-10～AC-15 | 不满足 | Vue 最小长度为 6 且有字符限制；Java 新增参数没有对应必填和长度校验。 | `passwordRule.js` 的 `pwdValidator`；`SysUser.getPassword()`；`SysUserController.add()` |
| AC-16 | 满足 | 前后端均允许不填写手机号。 | `index.vue` 的 `rules.phonenumber`；`SysUser.getPhonenumber()` |
| AC-17、AC-18 | 不满足 | Vue 和 Java 的手机号格式规则均与 AC 不完全一致。 | `index.vue` 的 `rules.phonenumber`；`SysUser.getPhonenumber()` |
| AC-19 | 满足 | 新增后可通过列表查询，列表过滤未删除用户。 | `SysUserController.list()`；`SysUserMapper.xml` 的 `selectUserList` |
| AC-20 | 满足 | Vue 默认 `status="0"`，数据库字段默认值也是 `0`。 | `index.vue` 的 `reset()`；`ry_20260417.sql` 的 `sys_user.status` |
| AC-21、AC-22 | 满足 | 管理员删除普通用户链路存在，删除后列表过滤逻辑删除用户。 | `SysUserController.remove()`；`SysUserServiceImpl.deleteUserByIds()`；`SysUserMapper.xml` 的 `deleteUserByIds`、`selectUserList` |
| AC-23 | 不满足 | 详情查询没有过滤逻辑删除用户。 | `SysUserMapper.xml` 的 `selectUserById` |
| AC-24 | 满足 | Controller 明确阻止删除当前登录用户。 | `SysUserController.remove()` |
| AC-25 | 满足核心数据规则 | 删除 SQL 只更新指定用户 ID；不存在目标不会更新其他用户。 | `SysUserMapper.xml` 的 `deleteUserByIds` |

## 7. 建议优先确认和处理顺序

1. 优先修正或确认服务端新增校验：用户名 2～20、昵称必填、密码 5～20、手机号格式。
2. 明确用户名大小写不敏感应由应用层还是数据库排序规则保证，并固定可验证实现。
3. 修正详情查询对逻辑删除用户的过滤，以满足 AC-23。
4. 确认手机号唯一性和用户名 `@Xss` 是否属于本次需求之外但必须保留的全局规则。
5. 在生成自动化测试前，确定验证码处理方式、测试环境隔离方式和测试框架选型。
6. 自动化实现时调整 TC-016 为不存在的数值用户 ID，并为需要手机号的用例生成独立手机号。
