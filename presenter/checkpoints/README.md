# 讲师 Checkpoints（仅兜底）

本目录不是现场输入，也不是 Agent 的知识来源。正常演示过程中不要打开，不要把路径加入 Prompt，不要让 OpenCode 检索。

仅当某阶段模型超时、网络异常或输出严重偏离且无法在规定时间修正时，讲师才可展示对应 checkpoint，并应明确告诉观众这是提前准备的参考输出。

启用实际文件/代码兜底时，优先使用 `scripts/use-demo-fallback.sh <phase>`，确保在 `demo-live/FALLBACK_USED.md` 留痕。
