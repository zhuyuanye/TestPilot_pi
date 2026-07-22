# 兜底输出目录

该目录保存提前验证过的参考结果，只用于以下情况：模型超时、网络异常、生成代码无法及时修复或演示环境故障。

正常现场演示不得让 Agent 读取本目录；本轮实时产物应写入 `demo-live/outputs/`。

兜底内容：

1. `user-management-acceptance-criteria.md`
2. `user-management-test-cases.md`
3. `implementation-gap-analysis.md`
4. `test-execution-report.md`
5. `defect-report.md`
6. `review-signoff.md`
7. `ui-test-execution-report.md`

使用兜底时不要手工无痕复制，应执行：

```bash
./scripts/use-demo-fallback.sh <phase>
```

脚本会把使用时间、阶段和来源 commit 写入 `demo-live/FALLBACK_USED.md`。自动化兜底仍需现场重新执行，历史 `target/`、Trace、截图和 `.env` 不会被复制。
