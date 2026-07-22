#!/usr/bin/env bash
set -euo pipefail

: "${RUOYI_ADMIN_USERNAME:?请设置 RUOYI_ADMIN_USERNAME}"
: "${RUOYI_ADMIN_PASSWORD:?请设置 RUOYI_ADMIN_PASSWORD}"

export RUOYI_UI_URL="${RUOYI_UI_URL:-http://localhost:8081}"
export RUOYI_BASE_URL="${RUOYI_BASE_URL:-http://localhost:8080}"
export HEADLESS="${HEADLESS:-true}"
export SLOW_MO="${SLOW_MO:-0}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

mvn -f "$ROOT_DIR/automation/ui/pom.xml" test "$@"
