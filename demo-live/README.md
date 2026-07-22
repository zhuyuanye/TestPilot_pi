# 现场演示工作区

该目录用于 30 分钟现场演示时由 OpenCode 实时生成产物。

运行 `scripts/prepare-live-demo.sh --reset` 后会创建：

```text
demo-live/
├── outputs/
├── automation/api/
├── automation/ui/
├── logs/
└── SESSION.md
```

除本说明外，目录内容被 Git 忽略，避免把现场结果误当成预置结果。仓库根目录的 `outputs/`、`automation/` 和 `presenter/checkpoints/` 均为兜底材料；正常现场流程不得让 Agent 读取。

如果必须使用兜底，运行 `scripts/use-demo-fallback.sh <phase>`，脚本会在 `demo-live/FALLBACK_USED.md` 留下明确记录。
