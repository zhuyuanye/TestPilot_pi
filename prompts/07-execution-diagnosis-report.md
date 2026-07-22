# Prompt 07：现场证据核验、失败诊断与结论

```text
请只使用本轮 demo-live/ 中的代码、Surefire XML、日志、Trace/截图清单，以及本轮确认的 AC/TC。禁止读取仓库现有 outputs/、automation/ 和 presenter/checkpoints/。

先做证据核验，不要直接写结论：
1. 列出实际执行命令及退出码
2. 从 Surefire XML统计 tests/passed/failures/errors/skipped
3. 检查 UI Trace、两张成功步骤截图和失败截图是否真实存在
4. 计算证据文件大小和 SHA-256
5. 将每个自动化方法映射到 TC 和 AC
6. 区分产品缺陷、测试脚本缺陷、环境问题和未覆盖风险
7. 对每个失败展示“AC 预期—真实响应/页面—实现证据—分类”

发现缺少原始证据时标记“未证实”，不得根据之前的 Markdown 猜测通过。

证据核验后先在对话中展示摘要，等待我回复“执行结论确认”。确认后生成：
- demo-live/outputs/test-execution-report.md
- demo-live/outputs/defect-report.md
- demo-live/outputs/traceability-matrix.md

最终报告必须注明 Git commit、执行时间、环境地址（不含凭据）、原始证据路径和 SHA-256。不能为了得到绿色结论弱化断言。
```

## 现场可信性挑战（建议执行）

由讲师而不是 Agent 手动使用错误 UI 地址再运行一次：

```bash
RUOYI_UI_URL=http://localhost:9999 HEADLESS=true <本轮 UI 执行命令>
```

确认测试变红、退出码非零、失败截图和新 Trace 产生；随后恢复正确地址重新运行。让观众看到“正确时绿、故意破坏时红”，证明报告不是静态编造。
