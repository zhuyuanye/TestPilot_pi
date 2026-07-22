#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LIVE_DIR="$ROOT_DIR/demo-live"
PHASE="${1:-}"

usage() {
  echo "用法: $0 {ac|cases|analysis|api|ui|reports}" >&2
  exit 2
}
[[ -n "$PHASE" ]] || usage
mkdir -p "$LIVE_DIR/outputs" "$LIVE_DIR/automation"

case "$PHASE" in
  ac)
    cp "$ROOT_DIR/outputs/user-management-acceptance-criteria.md" "$LIVE_DIR/outputs/"
    ;;
  cases)
    cp "$ROOT_DIR/outputs/user-management-test-cases.md" "$LIVE_DIR/outputs/"
    ;;
  analysis)
    cp "$ROOT_DIR/outputs/implementation-gap-analysis.md" "$LIVE_DIR/outputs/"
    ;;
  api|ui)
    rm -rf "$LIVE_DIR/automation/$PHASE"
    mkdir -p "$LIVE_DIR/automation/$PHASE"
    rsync -a --exclude '.env' --exclude 'target/' "$ROOT_DIR/automation/$PHASE/" "$LIVE_DIR/automation/$PHASE/"
    ;;
  reports)
    cp "$ROOT_DIR/outputs/test-execution-report.md" "$LIVE_DIR/outputs/"
    cp "$ROOT_DIR/outputs/defect-report.md" "$LIVE_DIR/outputs/"
    cp "$ROOT_DIR/outputs/ui-test-execution-report.md" "$LIVE_DIR/outputs/"
    ;;
  *) usage ;;
esac

record="$LIVE_DIR/FALLBACK_USED.md"
if [[ ! -f "$record" ]]; then
  printf '# 现场兜底使用记录\n\n' > "$record"
fi
printf -- '- %s | phase=%s | source_commit=%s\n' \
  "$(date -Iseconds)" "$PHASE" "$(git -C "$ROOT_DIR" rev-parse HEAD)" >> "$record"

echo "[FALLBACK] 已启用 $PHASE 兜底，并记录到 demo-live/FALLBACK_USED.md"
