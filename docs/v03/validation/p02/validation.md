# P02 验证记录

日期：2026-08-21

## 自动测试

| 命令 | 结果 |
|---|---|
| `mvn -B -ntp test` | PASS，37 tests，0 failures，0 errors，0 skipped |
| `mvn -B -ntp package -DskipTests` | PASS |
| `git diff --check` | PASS |

Seed V2 专项 8 个测试覆盖：bundled 336 题校验、首次/重复、dry-run、完整字段、单题升级、INSERT_ONLY、坏 topic 回滚、重复 externalKey、重复 answerType、同版本 checksum 冲突、stale pack 和 USER 内容保护。

## 正式导入

| 项目 | 结果 |
|---|---|
| seedPack | `v03-core-complete` |
| version | `2026.08.21.2` |
| checksum | `a1f91d51d5fe1dbc687770bc3d88a0822eae4f1210327741599b5a56f301a5ec` |
| validate | valid=true，questions=336，warning=0 |
| dry-run | created=336，updated=0，不落库 |
| first import | created=336，updated=0 |
| repeat import | created=0，updated=0，skipped=336 |

## 数据库计数

```text
questions=336
star5=180
veryHigh=90
incompleteRequiredQuestionFields=0
answers=1008
followUps=1008
tagRelations=624
```

30 题稳定随机抽样见 `spot-check-30.csv`。每题均满足 fullFields=true、answers=3、followUps=3、tags>=1。

## 索引

专题筛选、启用状态、更新时间倒序分页查询执行 EXPLAIN 后命中：

```text
idx_question_topic_status_updated
```

验证工具：`scripts/SeedBankSpotCheck.java`。
