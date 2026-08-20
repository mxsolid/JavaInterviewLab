# P05 核心学习工作区验收

## API 与数据库

- `GET /api/v1/workbench`：首页首屏单请求，返回 overview、dueReviews、wrongQuestions。
- `GET /api/v1/knowledge-map`：分类、专题、题量、触达量、掌握量与状态均由 PostgreSQL 聚合。
- `GET /api/v1/questions/{id}`：仅返回题目元数据，不包含答案、讲解和追问参考答案。
- `GET /api/v1/questions/{id}/learning`：学习模式读取完整教学内容。
- `POST /api/v1/questions/{id}/answer-view`：显式披露答案；`question_answer_view` append-only，`(profile_id, client_view_id)` 幂等。
- answer-view 不更新 `study_progress`；attempt/progress/review 仍由既有同一事务提交链维护。

## 自动验证

| 范围 | 命令 | 结果 |
|---|---|---|
| 后端全量测试 | `mvn -B -ntp test` | 43/43 通过 |
| 后端打包 | `mvn -B -ntp package -DskipTests` | 通过 |
| 前端类型 | `npm run typecheck` | 通过 |
| 前端构建 | `npm run build` | 通过 |
| P05 E2E | `npm run e2e -- e2e/core-learning.spec.ts` | 3/3 通过 |
| 全量 E2E | `npm run e2e` | 7/7 通过 |

题目工作区集成测试覆盖：元数据无答案泄漏、学习内容完整返回、answer-view 首次与重复提交、跨题复用 UUID 冲突、数据库仅写一条查看记录、OpenAPI V1 路径存在。

浏览器验收覆盖：练习输出、显式披露、attempt 写库、刷新恢复、笔记 1 秒串行自动保存及刷新回读；console error、page error、非预期 4xx/5xx 均为 0。

## Visual baseline

- `screenshots/workbench-1720.png`
- `screenshots/knowledge-1720.png`
- `screenshots/question-1720.png`
- `screenshots/workbench-mobile-390.png`
- `screenshots/question-mobile-390.png`

人工检查未发现横向溢出、操作区遮挡或桌面布局截断。全量 shell 用例另覆盖 1440、1720、1920、390 四个视口。

## 已知限制

- 生产构建仍有共享 `PageHeader` chunk 超过 500 kB 的告警，按任务边界保留到 P08 性能阶段处理。
- `mvn clean test` 在清理阶段因 IntelliJ JPS 占用 `backend/target` 失败；未关闭用户 IDE。随后 `mvn test` 43/43 与跳测打包均通过。
