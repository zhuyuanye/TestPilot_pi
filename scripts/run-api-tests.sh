#!/usr/bin/env bash
set -euo pipefail

: "${RUOYI_USERNAME:?请设置 RUOYI_USERNAME}"
: "${RUOYI_PASSWORD:?请设置 RUOYI_PASSWORD}"

export RUOYI_BASE_URL="${RUOYI_BASE_URL:-http://localhost:8080}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

mvn -f "$ROOT_DIR/automation/api/pom.xml" test "$@"
