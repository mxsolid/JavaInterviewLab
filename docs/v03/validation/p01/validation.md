# P01 验证记录

日期：2026-08-21

## 自动验证

| 命令 | 结果 |
|---|---|
| `mvn -B -ntp test` | PASS，29 tests，0 failures，0 errors，0 skipped |
| `mvn -B -ntp package -DskipTests` | PASS，生成可执行 jar |
| `git diff --check` | PASS |

`mvn clean test` 在 `clean` 阶段无法删除 IntelliJ JPS 正占用的 `backend/target/classes/com/javainterviewlab/content/knowledge`。未关闭用户 IDE，也未使用更强删除方式。随后不清理产物执行完整测试和重新打包均通过。

## 并发不变量

- 同一 `profile_id + question_id`：第二事务在第一事务提交前无法取得锁。
- 同一 profile 的不同 question：第二事务可在第一事务持锁期间完成。
- 既有 append-only、客户端 UUID 幂等、错题历史次数、单 PENDING review、笔记及题目乐观锁未改变。

## 手工 HTTP 验证

本地 PostgreSQL `devdb`，后端端口 8080：

| 检查 | 结果 |
|---|---|
| `/api/v1/workbench` | success=true，totalQuestionCount=6，due=0，wrong=1 |
| `/api/v1/knowledge-map` | success=true，totalQuestionCount=6，categories=6 |
| `/api/dashboard` | success=true，旧入口可用 |
| `/v3/api-docs` | 包含 `/api/v1/workbench` 与 `/api/v1/knowledge-map` |

验收结束后后端已停止，8080 端口已释放。
