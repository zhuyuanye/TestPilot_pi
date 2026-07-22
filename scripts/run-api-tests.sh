#!/usr/bin/env bash
set -euo pipefail

: "${RUOYI_ADMIN_USERNAME:?请设置 RUOYI_ADMIN_USERNAME}"
: "${RUOYI_ADMIN_PASSWORD:?请设置 RUOYI_ADMIN_PASSWORD}"

export RUOYI_BASE_URL="${RUOYI_BASE_URL:-http://localhost:8080}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

mvn -f "$ROOT_DIR/automation/api/pom.xml" test "$@"
