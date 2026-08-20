# 开发变更记录

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
