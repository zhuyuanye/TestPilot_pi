# Prompt 04：现场对照需求与真实实现

```text
现在允许读取 /Users/zhuyuanye/Documents/Code/RuoYi-Vue，但仍禁止读取仓库现有 outputs/、automation/ 和 presenter/checkpoints/。

请对照：
- docs/requirements/user-management.md
- demo-live/outputs/user-management-acceptance-criteria.md
- demo-live/outputs/user-management-test-cases.md

定位并分析：
1. Vue 用户管理页面、表单规则和 API 封装
2. Java Controller、Service、领域对象和参数校验
3. 登录、用户新增、列表、详情和删除接口
4. Token 获取与传递方式
5. 用户名唯一性及大小写处理
6. 用户名、密码、手机号校验
7. 当前登录用户自删除保护
8. 测试数据查询与清理能力

每项必须分成三列：
- AC 要求
- 实现证据（文件路径 + 类/方法 + 关键逻辑）
- 差异、风险或待动态验证项

不要仅凭静态代码直接宣告测试通过，也不要修改业务源码或生成测试代码。
先在对话中展示关键证据和拟写结论，等待我抽查至少两个文件。只有我回复“实现分析评审通过”后，才写入 demo-live/outputs/implementation-gap-analysis.md。
```

## 人工控制点

现场打开 Agent 引用的 2～3 个源码位置核对，尤其检查用户名长度和逻辑删除详情查询，证明结论来自真实源码而非套话。
