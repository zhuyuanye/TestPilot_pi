# Checkpoint 04：需求与实现差异分析

## 实现位置

| 能力 | 实现位置 |
|---|---|
| 用户接口 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysUserController.java` |
| 登录接口 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysLoginController.java` |
| 后端字段校验 | `ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysUser.java` |
| 唯一性与删除业务 | `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysUserServiceImpl.java` |
| 用户 SQL | `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml` |
| Vue 表单及校验 | `ruoyi-ui/src/views/system/user/index.vue` |
| Vue API | `ruoyi-ui/src/api/system/user.js` |

## 对照结论

| AC | 需求/澄清 | 当前实现 | 结论 |
|---|---|---|---|
| AC-03 | 用户名 2～20 位 | Vue 校验 2～20；后端只校验最大 30 | API 可绕过，存在差异 |
| AC-04 | 用户名唯一 | Controller 调用唯一性查询 | 已实现，需动态验证 |
| AC-05 | 唯一性忽略大小写 | SQL 使用 `=` | 取决于数据库排序规则，待运行验证 |
| AC-06 | 昵称必填 | Vue required；后端无 `@NotBlank`，数据库 NOT NULL | 后端缺少明确参数校验 |
| AC-07/08 | 密码必填且 5～20 位 | Vue 使用密码规则；后端未见对应 Bean Validation | API 可能绕过，需测试 |
| AC-10 | 手机号必须合法 | Vue 有正则；后端仅限制最大 11 位 | API 可绕过，存在差异 |
| AC-11 | 删除后列表不可见 | 逻辑删除，列表限定 `del_flag='0'` | 与外部行为一致 |
| AC-12 | 删除后详情不可见 | `selectUserById` 未限制 `del_flag` | 可能仍返回详情，需测试 |
| AC-13 | 禁止删除自己 | Controller 比较当前 userId 并拒绝 | 已实现 |
| AC-14 | 不影响其他用户 | 批量逻辑删除指定 ID | 仍需用对照用户动态验证 |

## 不应转化为缺陷的实现规则

- 后端额外检查手机号唯一性，但需求未规定。应先作为需求/产品确认项。
- 删除采用逻辑删除属于实现方式，只要满足外部可见验收标准即可。
- 是否分配部门、岗位、角色在当前需求中未规定，不能擅自形成失败断言。
