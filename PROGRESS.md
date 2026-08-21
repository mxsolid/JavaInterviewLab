# Java Interview Lab 开发进度

> Agent 每次开始和结束任务都要更新本文件。不要删除历史完成记录。

## 状态说明
- TODO：未开始
- IN_PROGRESS：进行中
- BLOCKED：阻塞
- DONE：已完成

## V0.1 题库基础版

| ID | 子任务 | 状态 | 难度 | 相对工作量 | 建议 Agent Token 预算 |
|---|---|---|---|---|---|
| A01 | 初始化目录、Git、基础文档 | DONE | ★★ | 短 | 8k–20k |
| A02 | Spring Boot 后端骨架 | DONE | ★★★ | 中 | 15k–35k |
| A03 | React 前端骨架 + 白色主题 | DONE | ★★★ | 中 | 15k–35k |
| A04 | PostgreSQL + Flyway 基线 | DONE | ★★★ | 中 | 15k–35k |
| A05 | 分类 / 专题 / 标签 API | DONE | ★★★ | 中 | 15k–35k |
| A06 | 题目 / 多层答案 / 追问 API | DONE | ★★★★ | 中 | 20k–45k |
| A07 | 题库管理 CRUD 页面 | DONE | ★★★★ | 中 | 20k–45k |
| A08 | 题库学习详情页 | DONE | ★★★★ | 中 | 25k–55k |
| A09 | 搜索 / 筛选 / 分页 | DONE | ★★★ | 中 | 15k–30k |
| A10 | 英文术语发音组件 | DONE | ★★ | 短 | 5k–15k |
| A11 | Seed 幂等导入 | DONE | ★★★ | 中 | 15k–30k |
| A12 | V0.1 联调 / 测试 / 验收 | DONE | ★★★★ | 中 | 20k–50k |

## V0.1.1 工程质量整改

| ID | 子任务 | 状态 |
|---|---|---|
| Q01 | Git 完整性、代码格式、MyBatis XML、OpenAPI、Seed 上传、文档与测试整改 | DONE |

## V0.2 学习闭环版

| ID | 子任务 | 状态 | 难度 | 相对工作量 | 建议 Agent Token 预算 |
|---|---|---|---|---|---|
| B01 | 学习路线 / 每日计划模型 | DONE | ★★★ | 中 | 15k–35k |
| B01.1 | 注释、枚举、Repository 边界、B01 同步和前端细节整改 | DONE | ★★★★ | 中 | 25k–55k |
| B02 | 答题记录 append-only | DONE | ★★★ | 中 | 15k–35k |
| B03 | 掌握度 + 进度事务更新 | DONE | ★★★★ | 中 | 20k–45k |
| B04 | 错题本 / 收藏 | DONE | ★★★ | 中 | 15k–30k |
| B05 | 笔记 + 自动保存 + 乐观锁 | DONE | ★★★★ | 中 | 20k–45k |
| B06 | 间隔复习调度 | DONE | ★★★★ | 中 | 20k–45k |
| B07 | 首页学习进度仪表盘 | DONE | ★★★★ | 中 | 20k–50k |
| B08 | V0.2 数据可靠性测试 | DONE | ★★★★ | 中 | 20k–50k |

## V0.2.1 稳定性与可用性优化

| ID | 子任务 | 状态 |
|---|---|---|
| BE01 | 复习逾期语义与 Dashboard 统计 | DONE |
| BE02 | 幂等响应与单题进度接口 | DONE |
| BE03 | 笔记首次并发创建 | DONE |
| BE04 | Service/DTO/注释边界整理 | DONE |
| BE05 | 测试、文档、Smoke 验收 | DONE |
| FE01 | API Client / 类型 / queryKey | DONE |
| FE02 | AppShell / Design System / 导航 | DONE |
| FE03 | PracticePanel 答题闭环 | DONE |
| FE04 | 题目详情学习体验 | DONE |
| FE05 | 复习中心 | DONE |
| FE06 | Dashboard | DONE |
| FE07 | NoteEditor / 收藏 / 错题交互 | DONE |
| FE08 | StudyPlan / QuestionBank 原型统一 | DONE |
| FE09 | 本地构建与端到端验收 | DONE |

## V0.3 产品化阶段

| Phase | 任务 | 状态 |
|---|---|---|
| P00 | 锁定基线、复现问题、建立验收证据 | DONE |
| P01 | 后端稳定性与 API 契约 | DONE |
| P02 | Seed Import V2 与完整题库 | DONE |
| P03 | Scenario / Source / Lab 后端 | DONE |
| P04 | 前端 Design System 与 AppShell | DONE |
| P05 | Workbench / Knowledge / Question 核心页面 | DONE |
| P06 | Scenario / Source / Lab / Interview 页面 | DONE |
| P07 | 真实端到端联调 | DONE |
| P08 | E2E / Visual / Performance | DONE |
| P09 | Windows 部署与最终验收 | DONE |

## V0.3 场景训练版

| ID | 子任务 | 状态 | 难度 | 相对工作量 | 建议 Agent Token 预算 |
|---|---|---|---|---|---|
| C01 | 场景 / Case / Solution 数据模型 | TODO | ★★★★ | 中 | 20k–45k |
| C02 | 场景 CRUD | TODO | ★★★ | 中 | 15k–35k |
| C03 | Case 学习页 | TODO | ★★★★ | 中 | 20k–50k |
| C04 | 方案比较矩阵 | TODO | ★★★★ | 中 | 20k–50k |
| C05 | 关联题目 / 知识 / 设计模式 | TODO | ★★★★ | 中 | 20k–45k |
| C06 | 场景追问树 | TODO | ★★★ | 中 | 15k–35k |
| C07 | 支付重复处理完整示范内容 | TODO | ★★★★ | 长 | 30k–70k |
| C08 | 秒杀 / 订单超时 / 缓存一致性基础种子 | TODO | ★★★★ | 长 | 30k–80k |
| C09 | V0.3 联调 / 验收 | TODO | ★★★★ | 中 | 20k–50k |

> Token 为任务规划预算，不是硬性消耗；Agent 反复重读全仓库、无关重构、一次生成大量题库会显著增加消耗。

## 当前阻塞

- 无。V0.2 已完成本地 PostgreSQL 自动测试、前端构建和停启后的学习数据回读验收。

## 当前决策

- P00 基线锁定为 `b99e7fb55995162d301d3690c14c3791beaef6c3`，后续 phase 基于 `feat/v03` 继续。
- 根目录 `AGENTS.md` 的日期格式 `yyyy-MM-dd HH:mm:ss` 高于旧启动包中的错误格式写法。
- 日志继续遵守根目录 `AGENTS.md`，不记录密码、Token 或完整敏感数据；dev 环境不降低该约束。
- P01 使用 `CurrentProfileProvider` 隔离当前档案解析；V0.3 仍由默认档案实现，后续账户上下文不进入现有业务服务。
- 进度并发锁调整为 PostgreSQL `profile_id + question_id` 事务级 advisory lock；同题串行，不同题可并行。
- `/api/v1/workbench` 与 `/api/v1/knowledge-map` 作为 V0.3 新契约；V0.2 `/api/dashboard` 继续兼容。
- P02 生产 seed 使用 `v03-core-complete/2026.08.21.2`，checksum 为 `a1f91d51d5fe1dbc687770bc3d88a0822eae4f1210327741599b5a56f301a5ec`。
- 启动包原题库有 48 道题标签重复，规范化时去重并升级版本；未放松重复标签校验。
- 启动包 1008 条追问只有标题。生产 seed 转为 V2 对象并将 `referenceAnswer` 保持 `null`，不编造内容；importer 对真实 referenceAnswer 的写入由集成测试覆盖。
- P03 场景包的 12 种方案定义跨 12 个场景完全一致，数据库按全局方案词典保存 12 条，通过 Case 关系驱动矩阵，避免复制 144 条同义记录。
- P03 保留旧基线已存在的空 Scenario 表并用 V12/V13 增量升级；`scenario_attempt` 继续采用 append-only 与 `(profile_id, client_attempt_id)` 幂等约束。
- Source 只录入 3 段项目自写教学伪代码；Interview 本阶段只提供持久化结构与 evaluator 接口，不伪造外部模型能力。
- P04 统一采用 canonical 的 `#f0f4f9/#0284c7/#3b82f6` 主色体系；不再使用 `#5b6cf8` 作为品牌主色。
- TopBar 路线选择读取并提交真实 StudyPlan API；GlobalSearch 只把用户输入传给题库 `keyword` 参数，不在 shell 造搜索结果。
- OpenAPI JSON 与生成的 TypeScript schema 一并版本化；`npm run openapi:generate` 可离线复现，现有学习路线类型已接入生成契约。
- P04 Playwright 使用本机 Chrome channel；四视口 screenshot baseline 固定 reduced motion，避免动画像素漂移。
- P05 练习模式首屏只读取 `/api/v1/questions/{id}` 元数据；学习模式或显式 answer-view 后才返回教学内容，避免参考答案在首屏响应泄漏。
- `question_answer_view` 采用 append-only 与 `(profile_id, client_view_id)` 幂等约束；查看答案不推进掌握度，只有 attempt 提交更新 progress/review。
- Workbench 首屏收敛到单个 `/api/v1/workbench` 请求；错题操作后失效聚合查询，Knowledge Map 在答题提交后同步失效。
- P06 场景提交继续复用 append-only `scenario_attempt` 与 UUID 幂等约束；前端候选关系和方案矩阵只读取数据库 API。
- Source 片段通过 V16 关联真实 Topic；代码查看器只展示项目自写教学伪代码，不复制第三方完整源码。
- Lab 固定为五个纯 TypeScript 状态机，后端只提供版本化定义；前后步骤不会产生数据库写入。
- Interview 基线采用本地可解释规则评分，四维权重固定为准确性 40、深度 25、结构 20、示例 15；外部 provider 仅保留关闭的 feature flag，不伪造 AI 能力。
- P07 增加 `/api/v1/system/status`，由同一 PostgreSQL 只读快照返回 Flyway、题目、场景、Source 和 Lab 状态；管理页不再显示静态系统数字。
- fresh DB 验收只创建并删除正则校验后的 `jil_p07_*` 随机数据库；不清理或改写 `devdb`。
- 三条学习路线的 34 个目标全部改为正式 V0.3 Seed externalKey；导入 336 题后必须全部解析写入 `study_plan_item`。
- P07 happy path 禁止 Mock；404、笔记 409、Seed 422、同 UUID 重试均调用真实后端。backend-down 通过真实停止 8080 后端验证错误页。
- P08 Playwright 每次重建固定隔离库 `jil_e2e`，使用 `local,e2e` profile 导入 336 题并创建独立档案；不复用 `devdb` 的历史状态。
- 视觉回归固定 11 个规定视口，像素差异比例门限为 0.001；旧阶段 screenshot smoke 不覆盖 P04～P06 历史证据。
- 生产构建以 850 KiB 首页初始静态 JS 和 500 KiB 单 chunk 为门限；首页必须保持不加载 Lab/Source 路由模块。
- keyword search 在 336 题上的执行时间为 0.700 ms；当前规模保留顺序扫描，不为 P08 增加 trigram 索引。
- Source 固定 Topic 映射在 Seed 导入事务中补齐，解决 Flyway V16 早于 Topic 导入时的新库空关联。
