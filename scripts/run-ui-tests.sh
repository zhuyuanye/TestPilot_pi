#!/usr/bin/env bash
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/automation/ui/.env"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

: "${RUOYI_UI_URL:?请设置 RUOYI_UI_URL}"
: "${RUOYI_BASE_URL:?请设置 RUOYI_BASE_URL}"
: "${RUOYI_ADMIN_USERNAME:?请设置 RUOYI_ADMIN_USERNAME}"
: "${RUOYI_ADMIN_PASSWORD:?请设置 RUOYI_ADMIN_PASSWORD}"

export HEADLESS="${HEADLESS:-true}"
export SLOW_MO="${SLOW_MO:-0}"

rm -f "$ROOT_DIR"/automation/ui/target/surefire-reports/TEST-*.xml
mvn -f "$ROOT_DIR/automation/ui/pom.xml" test "$@"
test_status=$?

python3 "$ROOT_DIR/scripts/export-ui-result.py"
exit "$test_status"
