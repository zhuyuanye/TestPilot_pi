# Prompt 02：生成并评审 AC

先将 `presenter/product-clarifications.md` 中的澄清内容作为产品回复提供给 OpenCode，再执行：

```text
产品已经回复了上一阶段的问题。请结合：
- docs/requirements/user-management.md
- 刚刚提供的产品澄清结果

先检查是否仍有阻塞验收的未决问题或冲突。如果存在阻塞项，只列出问题，不生成 AC；如果不存在，则生成验收标准。

AC 要求：
1. 使用 Given-When-Then 格式
2. 编号为 AC-01、AC-02……
3. 一条 AC 只表达一个主要业务规则
4. 覆盖正常、异常、边界、权限和数据一致性
5. 标注来源：原始需求章节或产品澄清编号
6. 不混入接口路径、数据库字段等实现细节
7. 不自行增加规则；未决内容标记为“待确认”
8. 附带“需求规则—AC”追踪矩阵

生成后再以评审者身份检查：
- 可追溯性
- 可测试性
- 重复或遗漏
- 是否含主观、模糊预期
- 是否错误引入实现细节

将评审后的最终结果写入 outputs/user-management-acceptance-criteria.md。
不要生成测试用例和代码。
```
