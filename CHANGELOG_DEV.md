# 开发变更记录

## 2026-08-21 — V0.3 P03 Scenario / Source / Lab 后端

- 新增 Flyway V12/V13，增量升级旧 Scenario 草案表，并建立场景作答、种子历史、Source、Lab 和 Interview 持久化结构。
- 导入 `v03-scenarios-complete/2026.08.21.1`：12 个场景、60 个 Case、12 个全局方案、140 个矩阵关系；同版本重复导入按 checksum 幂等返回。
- 新增场景列表、详情、方案矩阵和 attempt 提交 API；矩阵完全由数据库关系生成，attempt 采用 UUID 唯一约束且只追加。
- 新增 Source 列表/详情与 3 段项目自写教学伪代码；新增 3 个 Lab 定义元数据 API。
- Interview 建立 session/turn 表与 evaluator 接口，不接外部 LLM，不提供伪造评分。

### 验证

- JDK 21 + Maven 3.8.4：`mvn -B -ntp test` 通过，40 个测试；`mvn -B -ntp package -DskipTests` 通过。
- 本地 HTTP：12 个场景、详情 5 个 Case/12 个方案、矩阵 15 个关系单元、Source 3 条、Lab 3 条均可读取。
- 同一 `clientAttemptId` 连续提交首次 `duplicated=false`、第二次 `duplicated=true`；OpenAPI 包含全部 P03 路径。

## 2026-08-21 — V0.3 P02 Seed Import V2 与 336 题核心题库

- 新增 Flyway V11：题目记录 `seed_pack/source_version`，新增 checksum 导入历史和专题分页复合索引。
- 新增 `/api/v1/system/seeds/validate` 与 `/import?dryRun=`；保留旧 `/api/system/seeds/import`。
- Seed V2 支持 SHA-256、INSERT_ONLY/UPSERT、同版本幂等、旧版本拒绝、同版本 checksum 冲突、10MB/5000 题边界和严格重复字段检测。
- UPSERT 只更新 BUILTIN 或同 seed namespace 的 IMPORTED 题；数据库条件再次阻止 USER/异 namespace 覆盖。答案、追问、标签按快照替换，学习历史不删除。
- 完整导入 commonMistakes、scorePoints、sourceVersion、answers、tags 和 followUp referenceAnswer。
- 将 336 题生产 seed 纳入 resources。validate 发现原包 48 道题标签重复后进行内容修正，版本升级为 `2026.08.21.2` 并重算 checksum。
- 原包 1008 条追问没有参考答案；规范化后显式保存 `referenceAnswer=null`，不为了填字段编造内容。

### 验证

- JDK 21 + Maven 3.8.4：`mvn -B -ntp test` 通过，37 个测试；`mvn -B -ntp package -DskipTests` 通过。
- 必测覆盖首次/重复、单题升级、INSERT_ONLY、坏专题引用回滚、重复 externalKey、重复 answerType、checksum 冲突、旧包和 USER 保护。
- 正式导入：created=336、updated=0；相同版本重复导入 created=0、updated=0、skipped=336。
- 数据库：336 题、180 道五星、90 道 VERY_HIGH、1008 条答案、1008 条追问、624 条标签关系，要求的题目内容字段缺失 0。
- 稳定随机抽样 30 题全部 fullFields=true、3 层答案、至少 3 条追问且有标签；EXPLAIN 命中 `idx_question_topic_status_updated`。

## 2026-08-21 — V0.3 P01 后端稳定性与 API 契约

- 新增 `CurrentProfileProvider`，统一当前学习档案解析，移除答题、进度、复习、笔记、收藏、Dashboard 和路线服务中的重复默认档案查询。
- 进度提交改为 `profile_id + question_id` 粒度的 PostgreSQL 事务锁；同题提交串行，不同题目可并行。
- 新增 `/api/v1/workbench`，复用既有 Dashboard、待复习和错题口径，所有数据来自 PostgreSQL。
- 新增 `/api/v1/knowledge-map`，一次 SQL 聚合启用分类、专题、题量和当前档案掌握状态，避免 N+1 和大文本列表查询。
- OpenAPI 发布两个 V0.3 路径；保留 `/api/dashboard` 兼容旧前端。

### 验证

- JDK 21 + Maven 3.8.4：`mvn -B -ntp test` 通过，29 个测试；`mvn -B -ntp package -DskipTests` 通过。
- 并发测试：同题第二事务等待首事务提交；不同题目在首事务持锁期间可并行完成。
- 本地 HTTP：Workbench、Knowledge Map、旧 Dashboard 均返回成功；OpenAPI 包含两个 `/api/v1` 路径。
- `mvn clean test` 的清理阶段受 IntelliJ JPS 占用 `backend/target` 影响，本阶段未关闭用户 IDE；非清理测试和重新打包均通过。

## 2026-08-21 — V0.3 P00 基线锁定与复现证据

- 锁定 V0.3 基线 `b99e7fb55995162d301d3690c14c3791beaef6c3`，创建 `feat/v03` 分支；未修改业务代码。
- 保存后端测试/打包、前端安装/类型检查/构建、数据库版本与题目数量基线。
- 用真实浏览器打开 `/`、`/study`、`/questions`、`/questions/1`、`/review`，保存 5 张当前页面截图和 2 张 canonical 对照截图。
- 记录当前 5 个导航入口与 canonical 10 个模块的差距；确认业务请求无 4xx/5xx，存在 3 类 Ant Design 弃用 console error。
- 记录生产单 JS chunk 1,240.10 kB、gzip 393.52 kB，保留 Vite 超过 500 kB 告警作为后续性能基线。

### 验证

- JDK 21.0.12.1 + Maven 3.8.4：`mvn -B -ntp clean test` 通过，25 个测试；`mvn -B -ntp package -DskipTests` 通过。
- Node 22.13.0 + npm 10.9.2：`npm ci`、`npm run typecheck`、`npm run build` 通过。
- PostgreSQL 16.15：Flyway V10，题目 7 条，其中 ENABLED 6 条。
- 浏览器：5 个当前路由均成功打开；观察到的 18 次业务请求全部返回 200。

## 2026-08-20 — V0.2.1 前端学习闭环、原型还原与端到端验收

- FE01：API Client 安全处理网络错误、JSON 业务错误和非 JSON HTTP 错误；`ApiRequestError` 增加 `code`、`status`、`traceId`，学习 queryKey 集中管理。
- FE02：建立白色教育产品 Token、AppShell、公共 PageHeader/SectionCard/StatCard/学习 Tag；增加 `/review`，修复题目详情等子路由菜单选中。
- FE03/FE04：新增真实 PracticePanel。题目默认进入练习模式，按“自己回答 → 查看答案 → 四档结果 → 自评 → 提交”调用后端，成功显示 progress 与下次复习；技术术语按题目文本动态匹配。
- FE05/FE06：新增复习中心三 Tab；Dashboard 重做为学习入口、真实统计、待复习、错题和最近学习，明确区分“已练习”和“较熟练及以上”。
- FE07/FE08：NoteEditor 改为单请求串行保存，按 `VERSION_CONFLICT` 处理真实冲突；收藏/错题单行 loading 和统一缓存失效；StudyPlan 对 SCENARIO 明确禁用，题库显示中文枚举与统一卡片样式。

### 验证

- Node 22.13.0：`npm ci`、`npm run typecheck`、`npm run build` 通过；Vite 仅保留主包超过 500 KB 警告。
- JDK 21 + Maven 3.8.4：`mvn -B -ntp clean test` 通过，25 个测试全部通过；`mvn -B -ntp package -DskipTests` 通过。
- 本地服务：后端 `http://127.0.0.1:8080/actuator/health` 返回 `UP`，Vite `http://127.0.0.1:5173` 返回 200。
- 浏览器：真实提交 questionId 26 后 progress 从 1 次变为 2 次；重复同 UUID 请求确认只增加一次，第二次 `duplicated=true`；笔记自动保存后刷新可回读；`/review` 菜单与三 Tab 可访问。

## 2026-08-20 — V0.2.1 后端稳定性与可用性优化

- BE01：新增 `GET /api/study/reviews/due`，待处理复习统一为“逾期加今日”；列表返回 `overdue`，Dashboard 新增 `dueReviewCount`，保留旧 `todayReviewCount` 和 `/today` 兼容既有调用。
- BE02：同一 `clientAttemptId` 重试不再返回空快照，改为回读原 attempt、当前 progress、当前 pending review，并标记 `duplicated=true`；新增单题进度接口，无历史时返回 PREVIEW / UNKNOWN 默认状态与描述字段。
- BE03：笔记首次保存改用 `INSERT ... ON CONFLICT DO NOTHING RETURNING id`。唯一键竞争失败时回读已有记录，不覆盖先保存内容，不吞没其他数据库异常。
- BE04：提交 Service 内部改用 Entity 传递 progress 和 review；调度结果与复习列表响应拆分。清理任务编号型生产注释，保留 profile 行锁覆盖首次 progress 创建竞争。
- BE05：新增 due、单题默认进度、笔记重复首次创建集成测试；既有学习闭环测试改为按测试题断言，避免本地持久化验收数据干扰。

### 验证

- JDK 21 + Maven 3.8.4：`mvn -B -ntp clean test` 通过，25 个测试全部通过。
- JDK 21 + Maven 3.8.4：`mvn -B -ntp package -DskipTests` 通过，生成 Spring Boot 可执行 jar。
- 本地 HTTP（8080）：`/actuator/health` 返回 `UP`；Swagger UI 返回 HTTP 200；OpenAPI 包含 `/api/study/reviews/due` 与 `/api/study/questions/{questionId}/progress`；due、Dashboard 与单题进度只读 Smoke 通过。

## 2026-08-20 — B03-B08 V0.2 学习闭环与可靠性验收

- B03：新增 V7 `study_progress`、纯 Java 掌握度计算、profile 行锁和 attempt/progress 同事务提交；同 UUID 重试不重复推进快照。
- B04：新增 V8 `favorite`、错题查询与解决接口；错题状态只存在于 progress，默认列表隐藏停用题目。
- B05：新增 V9 `note`、1 秒防抖前端编辑器和 version 乐观锁；冲突返回 409，保存失败不会覆盖本地输入。
- B06：新增 V10 `review_task`、可配置固定间隔 `ReviewPolicy`；新答题完成旧 pending 后创建下一条，部分唯一索引保证同题只有一个 pending。
- B07：首页改为 PostgreSQL 聚合 Dashboard，分开显示路线时间和真实学习进度；五星掌握率口径固定为启用五星题中 SOLID/MASTERED 的占比。
- B08：新增掌握度、学习闭环、复习策略和事务回滚测试，以及只读重启检查脚本与验收报告。

### 验证

- JDK 21 + Maven 3.8.4：`mvn -B -ntp test` 通过，22 个测试通过；本地 PostgreSQL 16 从 V7 升级至 V10。
- Node 22.13.0：`npm run typecheck`、`npm run build` 通过；保留既有 Vite 主包超过 500 KB 警告。
- 已完成一次停服务后再启动的持久化检查：测试题目的 attempt、progress、favorite、note、review 均可通过 HTTP 回读；脚本位于 `scripts/05_v02_reliability_check.ps1`。

## 2026-08-20 — B02 Append-only 答题记录与 UUID 幂等

- 新增 Flyway V6：`question_attempt` 记录 profile、题目、客户端 UUID、答案查看状态、自评、结果类型、耗时和创建时间；对本地旧基线已存在的表采用补列迁移，保留历史数据。
- 新增 `AttemptResultType`、答题历史 Entity/Mapper/Service/Controller 和 `POST /api/study/attempts`。
- 同一 `profileId + clientAttemptId` 使用 PostgreSQL 唯一约束与 `ON CONFLICT DO NOTHING` 实现幂等；重复请求返回原记录，不重复写入，不记录答案正文日志。

### 验证

- JDK 21 + Maven 3.8.4：`mvn test` 通过，13 个测试全部通过，包含同 UUID 重试、不同 UUID、题目不存在及参数边界。

## 2026-08-20 — B01.1 代码质量、注释与学习路线边界整改

- content 与 study/plan 补齐关键 Controller、Service、Mapper、DTO、Entity、Row 和业务枚举的中文 Javadoc；六个既有内容枚举及新增 `StudyPlanTargetType` 均提供 `description`。
- 新增 `repository/model` 的 Entity / Row。分类、专题、标签、题目和学习路线 Mapper 不再返回 API Response DTO，也不再接收包含页面子项的题目请求 DTO；DTO 转换集中在 Service。
- 题目主表使用 `QuestionEntity`，列表、详情、标签、答案、追问使用只读 Row；学习路线查询同样使用 Row，REST 响应字段保持不变。
- `syncSystemPlans()` 改由应用启动和 Seed 导入后显式执行。所有路线 GET 接口只读；同步会清理目录已删除的当日项，未知 `targetType` 抛配置错误，未解析目标记录 WARN。
- 新增 `Clock` Bean 与路线时间进度计算器；Day 1、Day 2、超出路线封顶及未来开始时间均有单元测试。
- 新增 Flyway V5，删除被 UNIQUE / 部分唯一索引覆盖的 V4 重复索引。
- 学习路线页默认详情优先级调整为“用户选中 → 当前路线 → 首条路线”，专题项跳转题库专题筛选页，路线启动失败显示错误提示。

### 验证

- JDK 21 + Maven 3.8.4：`mvn test` 通过，11 个测试全部通过；真实 PostgreSQL `devdb` 已从 V4 迁移至 V5。
- Node 22.13.0：`npm run typecheck`、`npm run build` 通过；Vite 仍报告既有主包超过 500 KB 警告。
- 本地 HTTP（8080）：`/api/study/plans` 返回 3 条路线；`/api/study/current-plan` 和 `/api/study/today` 均返回 Day 1，今日包含 1 个关联项。

## 2026-08-20 — B01 学习路线 / 每日计划模型

- 新增 Flyway V4：默认学习档案、学习路线、逐日计划、计划项和当前路线选择；同一档案通过部分唯一索引限制为一条 active 路线。
- 提供 10 天突击、15 天推荐、30 天加强三条内置路线。路线定义由后端 JSON 幂等写入 PostgreSQL，题库 Seed 导入后自动补齐专题和题目关联。
- 新增 `/api/study/plans`、`/current-plan`、`/today` 和路线选择接口；`timeProgressDay` 仅表示自然时间，不冒充学习完成度。
- 前端 `/study` 已替换为路线选择、今日任务和逐日计划页面；Server State 由 TanStack Query 维护，queryKey 集中定义。
- 新增 B01 业务、数据库和接口文档，以及学习路线 API 集成测试。

### 验证

- `POSTGRES_PASSWORD=<本地测试密码>; mvn -B -ntp test`：5 个测试通过，包含路线写入、选择、当前路线和 Day 1 查询。
- `npm run build`（Node 22.13.0）：通过；保留第三方组件导致主包超过 500 KB 的既有构建警告。
- 本地 HTTP（8080）：路线列表返回 3 条；选择 15 天推荐后，当前路线和今日任务均返回 Day 1 集合框架概览及 1 个关联项。

## 2026-08-20 — V0.1.1 工程质量整改

- 全部 MyBatis SQL 已迁移至 `backend/src/main/resources/mapper`，题目列表改用 `QuestionQuery`，不再使用 `Object` 查询参数。
- 题目创建与更新 DTO 分离；更新继续通过 `version` 返回 409 保护并发编辑。
- Seed 导入改为 multipart JSON 上传，删除服务端文件路径参数；增加结构校验和导入日志。
- 新增 Springdoc OpenAPI、数据库/API/代码结构说明，README 更新为 V0.1 运行说明。
- 前端补齐 typecheck 脚本、2 空格规则与 multipart 导入调用；本机原有 features 文件将纳入 Git 跟踪。

## 2026-08-20 — A05～A12 V0.1 题库完成

- 新增分类、专题、标签的查询与管理 API；题目支持星级、难度、高频度、状态、多层答案、追问、标签与乐观锁更新。
- 新增题库分页筛选、题目管理、学习详情、统一英文术语朗读组件和本地 Vite API 代理。
- 新增 Flyway V2/V3、`seeds/v01-core.json` 及 JSON 幂等导入；使用 `seedPack`、`version` 和题目 `externalKey` 防重。
- 题目子项采用同一事务的全量替换，避免编辑后遗留被删除的答案、追问或标签；内置内容通过状态停用，不做物理删除。

### 验证

- `POSTGRES_PASSWORD=123456; mvn -B -ntp test`：4 个测试通过，含真实 PostgreSQL 的分类、专题、题目新增与旧版本冲突验证。
- `npm run build`（Node 22.13.0）：通过。
- 本地 HTTP 联调（8081）：重复导入 `v01-core.json` 跳过 5 条；题目创建、版本更新、详情读取、关键字与星级筛选均通过。
- Vite 仅保留第三方组件造成的主包大于 500 KB 警告，不影响构建或功能。

## 2026-08-20 — A04 PostgreSQL 与 Flyway 基线

- 新增 V0.1 内容域 V1 Flyway migration。
- local profile 从环境变量读取数据库密码，并对已执行草案的 devdb 启用 Flyway baseline。
- Maven 测试通过；Flyway 已在 PostgreSQL 16 的 devdb 建立版本 1 基线。

## 2026-08-20 — traceId 格式收紧

- traceId 统一为 6 位大写英文字母和数字。
- 保持 JDK 随机生成，不为该单点功能新增 Hutool 依赖。
- `mvn -B -ntp test`：3 个测试全部通过。

## 2026-08-20 — A03 React 前端骨架与白色主题

### 完成内容

- 创建 React 19、TypeScript、Vite 8、React Router、Ant Design 和 TanStack Query 工程。
- 提供统一主题 Token、AppShell、首页空骨架、Loading / Empty / Error 状态组件和 API Client。
- 页面只展示未接入数据的空状态，不加入题库 mock 数据。

### 验证

- 使用已安装的 Node 22.13.0 直接执行 TypeScript 与 Vite 构建：通过。
- 本机默认 Node 18 保持不变；Vite 8 构建需显式使用 `D:\Develop\node\nvm\v22.13.0\node.exe`。

## 2026-08-20 — A03 环境核验

### 结果

- 未创建前端工程。
- 本机 Node.js 为 18.20.8；Vite 8 和 @vitejs/plugin-react 要求 Node `^20.19.0 || >=22.12.0`。
- 为避免用不兼容运行时生成不可验证工程，A03 已标记 BLOCKED。

### 后续处理

- 已确认 Node 22.13.0 已通过 NVM 安装；前端构建将显式使用该版本，不切换全局 Node 18。

## 2026-08-20 — 基础设施修复

### 完成内容

- `favicon.ico` 缺失改为普通 `RESOURCE_NOT_FOUND` 响应，不再写入未处理异常日志。
- 生成的 traceId 改为 6 位英文数字；非法上游 traceId 自动替换为该格式。

### 验证

- `mvn -B -ntp test`：通过，3 个测试全部通过。
- 新增用例覆盖 `favicon.ico` 的 404 响应和生成 traceId 的 6 位格式。

## 2026-08-20 — A02 Spring Boot 后端骨架

### 完成内容

- 创建 Java 21、Spring Boot 3.5.16、Maven 后端工程。
- 引入 Web、Validation、Actuator、MyBatis、PostgreSQL、Flyway 和测试依赖。
- 提供统一 `ApiResponse`、错误码枚举、全局异常处理和受控业务异常。
- 为每个 HTTP 请求生成或透传合法 `X-Trace-Id`，同时写入 MDC、响应头和 API 响应。
- 提供 local profile 示例和 `/actuator/health`；A04 前暂时排除数据源自动配置，避免未完成数据库基线阻断服务启动。

### 设计取舍

- 统一异常只返回安全消息，完整异常仅记录在服务端日志，并携带 traceId。
- traceId 仅接受限定字符集和长度的上游值，避免请求头直接进入日志或响应头。
- A02 不创建业务 Controller、Service 或 Mapper，内容域 API 留给 A05 之后实现。

### 验证

- `mvn -B -ntp -gs D:\Develop\maven\apache-maven-3.8.4-2\conf\settings.xml -Dmaven.repo.local=<临时目录> test`：通过，2 个测试全部通过。
- 本地启动后以 `-NoProxy` 直连 `GET http://127.0.0.1:8080/actuator/health`：HTTP 200，返回 `status=UP`。
- 默认 Maven 全局 settings 激活的私有镜像 `10.219.23.13` 不可达；验证时只临时切换到现有公开镜像 settings，未修改全局配置。

## 2026-08-20 — A01 初始化目录、Git、基础文档

### 完成内容

- 初始化 Git 仓库，默认分支为 `main`。
- 将启动包的文档、脚本、数据库草案、种子和原型复制到项目根目录。
- 新增 `.editorconfig`、`.gitignore` 和后端、前端目录占位文件。
- 保留 `Agent_Starter_Pack` 作为本地参考，并通过 `.gitignore` 排除，避免重复纳入版本控制。

### 设计取舍

- A01 只建立工程地基，不提前创建 Spring Boot 或 React 业务代码，后续分别由 A02、A03 完成。
- 本地数据库凭据不写入 README 或 Git 配置；运行配置由后续 local profile 和环境变量承载。

### 验证

- `git init --initial-branch=main`：通过。
- JDK 21 与 Maven 3.8.4 版本检查：通过。
- `git diff --cached --check`：通过。
- `.gitignore` 对 `Agent_Starter_Pack` 的排除检查：通过。
- A01 未创建 Maven 或 Node.js 工程，因此本任务无编译、自动测试和接口手工测试。
