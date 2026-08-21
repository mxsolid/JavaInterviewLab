# P08 E2E / Visual / Performance 验收

验收日期：2026-08-21

## 确定性环境

- `scripts/07_e2e_backend.ps1` 每次只重建固定隔离库 `jil_e2e`，不会清理或改写 `devdb`。
- `local,e2e` profile 启动后导入正式 `v03-core-complete` 336 题，并创建独立 Profile、激活 10 天冲刺路线。
- Playwright 固定单 worker；后端与 Vite 均由测试配置托管，避免复用残留服务或数据。
- 修复全新库中 Flyway 先于 Topic Seed 执行造成的 3 条 Source 空关联；新库测试已断言 3/3 Source 均有 Topic。

## E2E 与 Visual Regression

- `npm run e2e`：17/17 通过，耗时 1.7 分钟。
- 11 个规定视口使用 `toHaveScreenshot` 像素基线，允许差异比例 `0.001`；无更新模式重复运行 3/3 通过。
- 覆盖 Workbench 1440×1000、1720×1100、1920×1080、390×844。
- 覆盖 Knowledge、Question、Scenario、Source、Lab、Review 1720×1100，以及 Question 390×844。
- 已人工检查 11 张基线；页面顶部、桌面品牌区、移动底部导航和首屏主操作均可见，无未解释 visual diff。
- 原 P04～P06 screenshot smoke 不再覆盖历史验收图；P08 基线集中保存在 `frontend/e2e/00-visual-regression.spec.ts-snapshots`。

## Accessibility smoke

- 键盘 Tab 覆盖链接、按钮和输入框，抽查 16 个焦点节点均有可见 focus indicator。
- 所有可见按钮均有可访问名称，未发现无名称 icon button。
- Knowledge 状态同时包含“未开始 / 学习中 / 已掌握”文本，不依赖颜色单独表达。
- `prefers-reduced-motion: reduce` 下页面可用，截图与交互稳定。

## Bundle gate

- `npm run performance`：通过。
- 首页初始静态 JS：834.51 KiB，门限 850 KiB。
- 最大单 chunk：174.26 KiB，门限 500 KiB。
- `/` 的静态依赖图和浏览器请求均未加载 Lab/Source 路由模块。
- 完整 chunk 表见 `bundle-report.md`。

## SQL

- 336 题上使用生产 Mapper 等价 keyword 查询执行 `EXPLAIN (ANALYZE, BUFFERS)`。
- `HashMap` 返回 7 行，执行时间 0.700 ms，shared hit 56。
- 当前规模的顺序扫描成本低，不增加写入成本更高的 trigram 索引。
- SQL 与执行计划见 `keyword-search-explain.md`。

## 完整验证

- JDK 21 + Maven 3.8.4：`mvn -B -ntp test` 通过，49/49。
- JDK 21 + Maven 3.8.4：`mvn -B -ntp package -DskipTests` 通过，生成可执行 JAR。
- Node 22.13.0：`npm run typecheck` 通过。
- Node 22.13.0：`npm run test` 通过，Vitest 5/5。
- Node 22.13.0：`npm run performance` 通过，生产构建与 bundle gate 均通过。
- Playwright Chromium：`npm run e2e` 通过，17/17。
