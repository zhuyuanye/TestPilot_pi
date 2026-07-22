# 测试执行证据规范

## 证据优先级

1. 测试框架机器可读结果：JUnit/Surefire XML、JSON、TAP 等。
2. 被测进程原始退出码。
3. 完整执行日志。
4. API 响应事实、浏览器 Trace、截图、录像或服务端日志。
5. Markdown 汇总报告。

Markdown 单独存在时不能证明测试执行过。

## 命令与退出码

使用 `tee` 时必须保存测试进程而不是 `tee` 的退出码：

```bash
<test-command> 2>&1 | tee <workspace>/logs/<run>.log
status=${PIPESTATUS[0]}
printf '%s\n' "$status" > <workspace>/logs/<run>.exit-code
```

命令记录中不得包含凭据。

## 证据清单

每个证据记录：

- 相对路径
- 用途
- 字节大小
- SHA-256
- 创建时间
- 关联 TC/AC

## API 证据

记录非敏感事实：请求方法、接口模式、HTTP 状态、业务状态、关键字段、动态数据标识和清理结果。禁止记录密码、完整 Token、Authorization Header 和无必要的个人信息。

## UI 证据

最少包括：

- 开启 snapshots/screenshots/sources 的 Trace
- 新增或关键状态验证后的成功截图
- 删除或状态迁移后的成功截图
- 失败现场截图
- 测试框架 XML 和原始退出码

Trace 可能包含敏感输入，只能保存在忽略目录或受控存储。

## 受控负向挑战

在安全环境中可使用错误的非敏感 URL、停止可恢复服务等方式验证测试能变红。必须恢复环境并重跑。该结果属于控制检查或环境失败，不能登记为产品缺陷。

## 结果状态

- `passed`：断言已执行并通过，原始证据完整。
- `failed`：断言已执行并失败，原始证据完整。
- `blocked`：未到达目标断言。
- `skipped`：测试框架明确跳过。
- `unverified`：原始证据不足。
