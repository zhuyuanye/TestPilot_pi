#!/usr/bin/env bash
set -uo pipefail

: "${RUOYI_ADMIN_USERNAME:?请设置 RUOYI_ADMIN_USERNAME}"
: "${RUOYI_ADMIN_PASSWORD:?请设置 RUOYI_ADMIN_PASSWORD}"

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export RUOYI_BASE_URL="${RUOYI_BASE_URL:-http://localhost:8080}"
export RUOYI_UI_URL="${RUOYI_UI_URL:-http://localhost:8081}"
export HEADLESS="${HEADLESS:-true}"
export SLOW_MO="${SLOW_MO:-0}"
API_TEST_PATTERN="${API_TEST_PATTERN:-UserManagement*ApiTest}"

"$ROOT_DIR/scripts/check-demo-env.sh" || exit 1

printf '\n=== API acceptance tests: %s ===\n' "$API_TEST_PATTERN"
mvn -f "$ROOT_DIR/automation/api/pom.xml" -Dtest="$API_TEST_PATTERN" test
api_status=$?

printf '\n=== Vue UI core flow ===\n'
mvn -f "$ROOT_DIR/automation/ui/pom.xml" test
ui_status=$?

printf '\n=== Summary ===\n'
printf 'API exit: %s\nUI exit:  %s\n' "$api_status" "$ui_status"
if (( api_status != 0 || ui_status != 0 )); then
  echo "Overall: FAILED（请根据 AC/TC 判断产品缺陷、环境问题或脚本问题）"
  exit 1
fi
echo "Overall: PASSED"
