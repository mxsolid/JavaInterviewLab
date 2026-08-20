# 13 API 接口说明

所有业务接口返回 `success`、`code`、`message`、`data` 和 `traceId`。参数非法为 400，资源不存在为 404，题目旧版本更新为 409。

| 模块 | 接口 |
|---|---|
| 分类 | `GET/POST/PUT /api/categories` |
| 专题 | `GET/POST/PUT /api/topics` |
| 标签 | `GET/POST/PUT /api/tags` |
| 题目 | `GET/POST /api/questions`、`GET/PUT /api/questions/{id}` |
| Seed | `POST /api/system/seeds/import`，`multipart/form-data` 的 `file` 字段 |

题目列表支持 `keyword`、`categoryId`、`topicId`、`starLevel`、`difficulty`、`frequencyLevel`、`status`、`page`、`pageSize`。默认页码为 1，默认每页 20 条，最大 100 条。

创建题目不传 `version`；修改题目必须传详情接口返回的 `version`。Swagger UI：`/swagger-ui.html`。
