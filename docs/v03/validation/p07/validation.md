# P07 验收记录

日期：2026-08-21

## Fresh DB

- 临时库命名：`jil_p07_<16位十六进制>`，创建和删除前均执行正则校验。
- Flyway：V1–V16 全部执行。
- 正式题库：`seed_pack=v03-core-complete`，336 题。
- 场景：12；Case：60。
- 学习路线：3 条路线共 34 个非空目标，全部解析写入 `study_plan_item`。
- Spring 上下文关闭后终止该临时库剩余连接并精确删除；`devdb` 不执行表清理。

## 真实联调与 SQL assertions

- Workbench、Knowledge Map、题库搜索、题目元数据、答案披露、Source、System Status 均通过真实 Controller/Service/PostgreSQL。
- 同一 question attempt UUID：首次 `duplicated=false`，重试 `duplicated=true`，`question_attempt` 只增加 1 条。
- `study_progress.wrong_count` 增加 1；同题 PENDING review 为 1；wrong book 激活。
- favorite、note 各 1 条；笔记过期 version 返回 409 `VERSION_CONFLICT`。
- 同一 scenario attempt UUID 只写入 1 条。
- 无效 Seed 返回 422 `CONTENT_VALIDATION_FAILED`；不存在题目返回 404 `RESOURCE_NOT_FOUND`。

## 自动验证

- `mvn -B -ntp test`：49/49 通过。
- `mvn -B -ntp package -DskipTests`：通过。
- `npm run typecheck`：通过。
- `npm run test`：5/5 通过。
- `npm run build`：通过。
- `npm run e2e`：14/14 通过。

## 手动失败验证

- 实际停止 8080 后端，保留 Vite 5173。
- Chrome 打开工作台后显示“学习工作台加载失败”，SideNav 与 TopBar 保持可用，无白屏。
- 截图：`backend-down.png`。
