# Prompt 04：需求、AC 与实现对照

目标源码路径已经确定。

```text
请分析 /Users/zhuyuanye/Documents/Code/RuoYi-Vue 下的 RuoYi-Vue 项目，并对照：
- docs/requirements/user-management.md
- outputs/user-management-acceptance-criteria.md
- outputs/user-management-test-cases.md

请定位并分析：
1. Vue 用户管理页面、表单校验和 API 请求封装
2. Java Controller、Service、领域对象及参数校验
3. 登录、用户新增、查询、详情和删除接口
4. Token 获取及传递方式
5. 用户名唯一性和大小写处理方式
6. 手机号、密码、用户名长度校验
7. 禁止删除当前登录用户的实现
8. 测试数据准备和清理所需能力
9. 项目现有测试框架和可复用工具

输出以下三类结论：
- 需求或 AC 明确规定
- 当前代码实际实现
- 差异、风险或待确认项

每个实现结论必须给出文件路径和关键类/方法，不要凭经验猜测。
本阶段不修改业务代码，不生成测试代码。
将结果写入 outputs/implementation-gap-analysis.md。
```
