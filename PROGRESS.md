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
