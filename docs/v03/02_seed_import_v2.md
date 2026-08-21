# V0.3 P02 Seed Import V2

## API

### `POST /api/v1/system/seeds/validate`

`multipart/form-data`，文件字段名 `file`。执行：

- 原始字节 SHA-256；
- JSON 重复属性检测；
- pack、题目、枚举、内容和危险标签校验；
- externalKey、追问标题和 tags 去重；
- category/topic 引用校验；
- 结合当前数据库计算 created/updated/skipped；
- 版本、checksum 和题目来源保护检查。

接口不写数据库。成功返回 `SeedValidationResponse`；内容错误返回 HTTP 422 `CONTENT_VALIDATION_FAILED`；同版本 checksum 冲突或旧版本返回 HTTP 409 `VERSION_CONFLICT`。

### `POST /api/v1/system/seeds/import?dryRun=true|false`

- `dryRun=true`：复用 validate 和数据库决策，不写数据库。
- `dryRun=false`：按 seedPack 取得事务 advisory lock，在单事务内写入 336 题、children、导入历史和路线同步。
- 相同 seedPack/version/checksum 已成功导入：created=0、updated=0，不执行任何写入。

旧 `POST /api/system/seeds/import` 保留，默认 `dryRun=false`。

## 更新语义

| 当前内容 | INSERT_ONLY | UPSERT |
|---|---|---|
| 不存在 | CREATE | CREATE |
| BUILTIN | SKIP | UPDATE |
| 同 seed namespace IMPORTED | SKIP | UPDATE |
| 其他 namespace IMPORTED | REJECT | REJECT |
| USER / FUTURE_AI | REJECT | REJECT |

UPDATE 会增加 `question.version`，并替换 answer、follow-up、tag 快照。attempt、progress、review、note、favorite 不参与删除或更新。

## 内容规范化

启动包原文件 checksum：`7ef5ac27de1fee94361fcb68372509da2203152b843b21e361631859c0b6c3c3`。

首次 validate 检出 48 道题的 topic tag 与同名 tag 重复。生产副本通过 `scripts/normalize_v03_seed_v2.mjs` 完成：

- 去除重复 tag；
- 增加 `mode=UPSERT`；
- 补齐 `questionType/originType/status`；
- 字符串追问转换为 V2 对象；
- 版本升级为 `2026.08.21.2`；
- 新 checksum：`a1f91d51d5fe1dbc687770bc3d88a0822eae4f1210327741599b5a56f301a5ec`。

原包没有提供追问参考答案。规范化对象将 `referenceAnswer` 保持 `null`，不生成无事实依据的内容。importer 对非空 referenceAnswer 的落库和升级由集成测试验证。

## 运行

后端启动后：

```powershell
& .\scripts\import-v03-bank.ps1
```

脚本依次执行 validate、dry-run、真实导入和重复导入，并校验四次 checksum 一致及重复导入零 mutation。
