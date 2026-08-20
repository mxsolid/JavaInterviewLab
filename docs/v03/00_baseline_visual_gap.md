# P00 基线与视觉差距

日期：2026-08-21
基线：`b99e7fb55995162d301d3690c14c3791beaef6c3`
分支：`feat/v03`
视口：1440 × 1100

## 基线结果

- 后端：JDK 21.0.12.1、Maven 3.8.4，`clean test` 通过 25 个测试，`package -DskipTests` 通过。
- 前端：Node 22.13.0、npm 10.9.2，`npm ci`、`typecheck`、`build` 通过。
- 数据库：PostgreSQL 16.15，Flyway V10；`question` 共 7 条，其中 ENABLED 6 条。
- 浏览器：`/`、`/study`、`/questions`、`/questions/1`、`/review` 均可打开；本轮观察到的业务请求全部返回 200。
- Console：存在 3 类 Ant Design 弃用信息，均以 `console.error` 输出：`Space.direction`、`List`、`Alert.message`。
- Production bundle：单个 JS chunk 为 1,240.10 kB，gzip 393.52 kB；Vite 报告超过 500 kB。

首次执行 `clean test` 时，遗留的本项目后端进程锁定 `backend/target/fe09-backend.err.log`，导致 clean 失败。确认 PID 的 jar 路径属于当前仓库并停止该进程后，完整重跑通过。失败与重试均保留在 `validation/p00/backend-test.txt`。

## 与 canonical prototype 的差距

### 产品壳与路由

- 当前左侧导航只有首页、开始学习、题库、复习中心、管理/设置 5 个入口。
- canonical prototype 定义 10 个入口。当前缺少知识地图、场景训练、源码阅读、动画实验室、模拟面试、AI 专题；管理入口也未形成内容、Seed、路线、系统状态的工作区。
- 当前顶部只有产品说明文字。canonical prototype 需要全局搜索、路线切换、快捷入口和用户状态。

### 首页 / 工作台

- 当前已使用真实 Dashboard、错题和待复习数据，Hero、快捷入口、统计、错题和最近学习可用。
- canonical prototype 的首屏更紧凑，含全局搜索、路线快捷切换、推荐攻坚和知识地图入口。当前首页没有知识聚合与推荐数据契约。
- 当前页面仍由 `/api/dashboard`、错题、待复习三个请求拼装，不是单一 Workbench 契约。

### 题目工作区

- 当前题目页具备练习/学习模式、收藏、真实进度、笔记、追问和术语发音，数据来自 API。
- 当前内容按单列卡片向下堆叠。canonical prototype 是主内容与右侧上下文的 8/4 工作区，并把掌握度、术语、追问、笔记固定在上下文栏。
- canonical prototype 的回答 Tab、闭卷输入、自评和源码联动在同一桌面视野内；当前练习态首屏不展示回答层级与源码联动。

### 学习路线、题库、复习

- 三个页面已有真实 API 与可操作内容，不是假数据页面。
- 视觉仍沿用 V0.2 通用 Ant Design 卡片和较宽留白，没有 canonical prototype 的顶部搜索、统一工作区密度与完整模块导航。
- 本轮仅锁定差距，没有修改 UI、API 或业务逻辑。

## 截图

- [当前首页](validation/p00/screenshots/home.png)
- [当前学习路线](validation/p00/screenshots/study.png)
- [当前题库](validation/p00/screenshots/questions.png)
- [当前题目 1](validation/p00/screenshots/question-1.png)
- [当前复习中心](validation/p00/screenshots/review.png)
- [Canonical 工作台](validation/p00/screenshots/canonical-workbench.png)
- [Canonical 题目工作区](validation/p00/screenshots/canonical-question.png)

## P00 边界

P00 只建立可复现基线。上述缺口由后续对应 phase 处理，本 phase 不改业务代码、不调整 API、不修复 UI。
