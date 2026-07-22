#!/usr/bin/env python3
"""Export Maven Surefire UI results to a versionable Markdown snapshot."""

from __future__ import annotations

import hashlib
import os
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPORT_DIR = ROOT / "automation" / "ui" / "target" / "surefire-reports"
OUTPUT = ROOT / "outputs" / "ui-test-execution-report.md"


def clean(value: str | None) -> str:
    return (value or "").replace("|", "\\|").replace("\n", " ").strip()


def main() -> int:
    files = sorted(REPORT_DIR.glob("TEST-*.xml"))
    rows: list[tuple[str, str, str, str]] = []
    totals = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}

    for file in files:
        suite = ET.parse(file).getroot()
        for key in totals:
            totals[key] += int(suite.attrib.get(key, "0"))
        for case in suite.findall("testcase"):
            status = "通过"
            detail = ""
            for tag, label in (("failure", "失败"), ("error", "错误"), ("skipped", "跳过")):
                node = case.find(tag)
                if node is not None:
                    status = label
                    detail = clean(node.attrib.get("message") or node.text)
                    break
            rows.append((
                clean(case.attrib.get("name")),
                status,
                case.attrib.get("time", "0"),
                detail,
            ))

    passed = totals["tests"] - totals["failures"] - totals["errors"] - totals["skipped"]
    overall = "通过" if files and totals["failures"] == 0 and totals["errors"] == 0 else "失败或未执行"
    evidence_root = ROOT / "automation" / "ui" / "target" / "evidence"
    evidence_files = sorted(path for path in evidence_root.rglob("*") if path.is_file())
    generated = datetime.now().astimezone().isoformat(timespec="seconds")
    ui_url = os.getenv("RUOYI_UI_URL", "http://localhost:8081")
    api_url = os.getenv("RUOYI_BASE_URL", "http://localhost:8080")

    lines = [
        "# Vue UI 自动化执行结果",
        "",
        f"> 由 `scripts/export-ui-result.py` 从 Maven Surefire XML 自动生成，时间：`{generated}`。",
        "",
        "## 执行结论",
        "",
        "| 项目 | 结果 |",
        "|---|---|",
        f"| 总体状态 | {overall} |",
        f"| Tests run | {totals['tests']} |",
        f"| Passed | {passed} |",
        f"| Failures | {totals['failures']} |",
        f"| Errors | {totals['errors']} |",
        f"| Skipped | {totals['skipped']} |",
        "",
        "## 场景追踪",
        "",
        "- 测试用例：TC-001",
        "- 验收标准：AC-01、AC-19、AC-20、AC-21、AC-22",
        "- 流程：管理员登录 → 新增用户 → 搜索确认 → 删除用户 → 确认列表消失",
        "- 数据清理：页面删除，并在 `@AfterEach` 中通过 API 兜底清理",
        "",
        "## 测试明细",
        "",
        "| 测试 | 状态 | 耗时（秒） | 失败摘要 |",
        "|---|---|---:|---|",
    ]
    if rows:
        lines.extend(f"| {name} | {status} | {duration} | {detail} |" for name, status, duration, detail in rows)
    else:
        lines.append("| 未找到 Surefire XML | 未执行 | 0 | 请先运行 `scripts/run-ui-tests.sh` |")

    lines.extend([
        "",
        "## 原始执行证据",
        "",
        "| 文件 | 大小（字节） | SHA-256 |",
        "|---|---:|---|",
    ])
    if evidence_files:
        for path in evidence_files:
            digest = hashlib.sha256(path.read_bytes()).hexdigest()
            relative = path.relative_to(ROOT).as_posix()
            lines.append(f"| `{relative}` | {path.stat().st_size} | `{digest}` |")
    else:
        lines.append("| 未生成 Trace 或截图 | 0 | - |")

    lines.extend([
        "",
        "> Trace 可能包含页面快照、请求信息及输入操作，仅用于本地或受控环境，不提交公开仓库。",
        "",
        "## 执行环境",
        "",
        f"- Vue 地址：`{ui_url}`",
        f"- API 地址：`{api_url}`",
        "- 框架：Playwright Java + JUnit 5 + Chromium",
        "- 原始临时结果：`automation/ui/target/surefire-reports/`（不提交 Git）",
        "- Trace 与截图：`automation/ui/target/evidence/TC-001-<timestamp>/`（不提交 Git）",
        "",
        "## 执行命令",
        "",
        "```bash",
        "HEADLESS=true ./scripts/run-ui-tests.sh",
        "```",
        "",
    ])

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"UI result exported: {OUTPUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
