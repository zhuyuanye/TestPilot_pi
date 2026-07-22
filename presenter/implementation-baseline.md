# RuoYi-Vue 实现基线（讲师材料）

## 版本与环境

- 源码：`/Users/zhuyuanye/Documents/Code/RuoYi-Vue`
- 分支：`master`
- 提交：`7da12b0c`
- RuoYi：3.9.2
- 后端：Java 17、Spring Boot 4.0.3
- 前端：Vue 2.6.12、Element UI 2.15.14
- OpenCode：1.18.4
- 当前本地后端：`http://localhost:8080`
- 当前验证码配置：关闭

> 源仓库存在未提交修改。本培训工程不修改 RuoYi-Vue，只创建外部黑盒 API 测试。

## 已确认接口

| 能力 | 方法与路径 |
|---|---|
| 验证码配置 | `GET /captchaImage` |
| 登录 | `POST /login` |
| 当前用户 | `GET /getInfo` |
| 用户列表 | `GET /system/user/list` |
| 用户详情 | `GET /system/user/{userId}` |
| 新增用户 | `POST /system/user` |
| 删除用户 | `DELETE /system/user/{userIds}` |

认证头：`Authorization: Bearer <token>`。

## 关键差异候选

1. 原始需求规定用户名 2～20 位；后端 `SysUser#getUserName` 只限制最大 30 位，未限制最小 2 位。
2. Vue 表单已经校验用户名 2～20 位，但输入框 `maxlength=30`；API 可绕过前端校验。
3. Vue 使用正则校验大陆手机号；后端 `SysUser#getPhonenumber` 只限制最大 11 位。
4. 需求规定昵称必填；后端昵称没有 `@NotBlank`，但数据库列为 `NOT NULL`。
5. 后端会检查手机号唯一性，但原始需求未规定手机号必须唯一。
6. 删除使用 `del_flag='2'` 逻辑删除，用户列表只查询 `del_flag='0'`。
7. Controller 明确阻止删除当前登录用户。
8. 用户名 SQL 使用等值比较；是否忽略大小写取决于数据库列排序规则，不能只凭 Java/SQL 代码下结论。

## 相关文件

- `ruoyi-admin/.../SysUserController.java`
- `ruoyi-admin/.../SysLoginController.java`
- `ruoyi-common/.../SysUser.java`
- `ruoyi-system/.../SysUserServiceImpl.java`
- `ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml`
- `ruoyi-ui/src/views/system/user/index.vue`
- `ruoyi-ui/src/api/system/user.js`
