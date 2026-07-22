#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${RUOYI_BASE_URL:-http://localhost:8080}"
RUOYI_PATH="${RUOYI_PATH:-/Users/zhuyuanye/Documents/Code/RuoYi-Vue}"

[[ -f "$RUOYI_PATH/pom.xml" ]] || { echo "[FAIL] 未找到 RuoYi-Vue: $RUOYI_PATH"; exit 1; }
echo "[OK] RuoYi-Vue 源码: $RUOYI_PATH"

response="$(curl --fail --silent --show-error --max-time 3 "$BASE_URL/captchaImage")" || {
  echo "[FAIL] 后端不可访问: $BASE_URL"
  exit 1
}
echo "[OK] 后端可访问: $BASE_URL"

python3 - "$response" <<'PY'
import json, sys
payload = json.loads(sys.argv[1])
if payload.get("code") != 200:
    raise SystemExit("[FAIL] captchaImage 业务状态异常")
if payload.get("captchaEnabled"):
    raise SystemExit("[FAIL] 验证码已开启，API 演示登录会被阻塞")
print("[OK] 验证码已关闭")
PY

command -v java >/dev/null || { echo "[FAIL] 未安装 Java"; exit 1; }
command -v mvn >/dev/null || { echo "[FAIL] 未安装 Maven"; exit 1; }
command -v opencode >/dev/null || { echo "[FAIL] 未安装 OpenCode"; exit 1; }
echo "[OK] Java: $(java -version 2>&1 | head -1)"
echo "[OK] Maven: $(mvn -version 2>&1 | head -1)"
echo "[OK] OpenCode: $(opencode --version)"

[[ -n "${RUOYI_USERNAME:-}" ]] || echo "[WARN] 未设置 RUOYI_USERNAME"
[[ -n "${RUOYI_PASSWORD:-}" ]] || echo "[WARN] 未设置 RUOYI_PASSWORD"
