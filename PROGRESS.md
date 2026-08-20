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
| P03 | Scenario / Source / Lab 后端 | TODO |
| P04 | 前端 Design System 与 AppShell | TODO |
| P05 | Workbench / Knowledge / Question 核心页面 | TODO |
| P06 | Scenario / Source / Lab / Interview 页面 | TODO |
| P07 | 真实端到端联调 | TODO |
| P08 | E2E / Visual / Performance | TODO |
| P09 | Windows 部署与最终验收 | TODO |

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
