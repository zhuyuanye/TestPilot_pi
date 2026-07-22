#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LIVE_DIR="$ROOT_DIR/demo-live"
RESET=false
[[ "${1:-}" == "--reset" ]] && RESET=true

if [[ -n "$(git -C "$ROOT_DIR" status --porcelain --untracked-files=normal)" ]]; then
  echo "[ERROR] Git 工作区不干净；请先提交、忽略或移除修改，避免现场结果与代码版本不一致。" >&2
  git -C "$ROOT_DIR" status --short >&2
  exit 1
fi

if [[ -n "$(find "$LIVE_DIR" -mindepth 1 -maxdepth 1 ! -name README.md -print -quit 2>/dev/null)" ]]; then
  if [[ "$RESET" != true ]]; then
    echo "[ERROR] demo-live 已有现场产物。确认不再需要后使用 --reset。" >&2
    exit 1
  fi
  find "$LIVE_DIR" -mindepth 1 -maxdepth 1 ! -name README.md -exec rm -rf {} +
fi

mkdir -p "$LIVE_DIR/outputs" "$LIVE_DIR/automation/api" "$LIVE_DIR/automation/ui" "$LIVE_DIR/logs"
commit="$(git -C "$ROOT_DIR" rev-parse HEAD)"
cat > "$LIVE_DIR/SESSION.md" <<EOF
# Live demo session

- Prepared at: $(date -Iseconds)
- Git commit: $commit
- Fallback used: no
EOF

printf '[OK] 现场工作区已清空并创建：%s\n' "$LIVE_DIR"
printf '[OK] 基线提交：%s\n' "$commit"
printf '[NEXT] 只打开原始需求和 prompts/01-requirement-analysis.md。\n'
