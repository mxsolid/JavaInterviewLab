# V0.3 P01 后端 API 契约

## 版本化入口

### `GET /api/v1/workbench`

响应 `ApiResponse<WorkbenchResponse>`：

- `generatedAt`：服务端生成时间，ISO-8601 时间戳。
- `overview`：复用既有 Dashboard 统计口径。
- `dueReviews`：当前档案逾期及今日待复习任务。
- `wrongQuestions`：当前仍激活的错题。

空数据返回空数组，不返回 `null`，不使用原型常量填充。

### `GET /api/v1/knowledge-map`

响应 `ApiResponse<KnowledgeMapResponse>`：

- `generatedAt`：服务端生成时间，ISO-8601 时间戳。
- `totalQuestionCount`、`touchedQuestionCount`、`masteredQuestionCount`：当前启用题目的聚合统计。
- `categories[].topics[]`：分类到专题的知识树。
- `masteryRate`：专题中 `SOLID` 或 `MASTERED` 题量除以启用题量，范围 `0..1`。
- `state`：`NOT_STARTED`、`LEARNING`、`MASTERED`。
- `stateDescription`：对应中文说明。

查询不读取题目和答案大文本。分类或专题无启用题目时仍保留节点，计数为 0。

## 错误与空状态

- 当前档案不存在：HTTP 404，`RESOURCE_NOT_FOUND`。
- 参数校验失败：HTTP 400，`VALIDATION_ERROR`。
- 内容导入校验失败：HTTP 422，`CONTENT_VALIDATION_FAILED`。
- 未处理异常：HTTP 500，`INTERNAL_ERROR`，响应保留 traceId。

## 兼容边界

- V0.2 `GET /api/dashboard` 保留，响应结构与统计口径不变。
- V0.3 新页面只依赖 `/api/v1` 新入口；旧页面可在迁移完成前继续调用旧入口。
- PostgreSQL 是唯一事实源；本契约不引入 Redis 或 MQ 运行依赖。
