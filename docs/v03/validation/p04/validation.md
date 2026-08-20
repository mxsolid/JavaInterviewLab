# V0.3 P04 验证记录

## 实现范围

- canonical token：背景 `#f0f4f9`，主蓝 `#0284c7`，accent `#3b82f6`，统一 radius/shadow/gap/motion。
- AppShell：224px SideNav、66px TopBar、真实学习路线切换、GlobalSearch、10 个模块入口。
- 响应式：980px 折叠导航；620px 以下使用 58px 移动底栏；支持 `prefers-reduced-motion`。
- 公共组件：PageHeader、SectionCard、StatCard、ProgressRing、StatusBadge、Loading/Empty/Error/PageSkeleton。
- 现有业务路由改为 React lazy loading；未完成模块显示明确 Empty 状态，不放生产假数据。
- OpenAPI：保存 `frontend/openapi/openapi.json`，由 `openapi-typescript` 离线生成 `src/api/generated/schema.ts`；学习路线类型已从生成 schema 派生。
- Playwright：使用本机 Chrome channel，固定视口、reduced motion 和截图路径。

## 自动验证

- Node 22.13.0 + npm 10.9.2：`npm ci` 通过。
- `npm run openapi:generate`：通过。
- `npm run typecheck`：通过。
- `npm run build`：通过。
- 懒加载后入口 JS 为 159.98 kB；仍有共享 `PageHeader` chunk 649.36 kB 的大包告警，留给 P08 性能门禁处理。
- `npm run screenshot:shell`：4/4 通过。

## 浏览器验证

- 视口：1440×900、1720×1000、1920×1080、390×844。
- 10 个导航入口逐页打开；console error 0，非预期 4xx/5xx 0，page error 0。
- GlobalSearch 键盘 Enter 跳转真实题库 `keyword` 参数；刷新后筛选值保留。
- Loading、Empty、Error 状态均在浏览器中验证。
- 人工对照 canonical：桌面 SideNav/TopBar/内容宽度一致；移动底栏位于真实首屏底部，无横向溢出。

## 证据

- `build.txt`
- `typecheck.txt`
- `playwright.txt`
- `screenshots/shell-1440.png`
- `screenshots/shell-1720.png`
- `screenshots/shell-1920.png`
- `screenshots/shell-mobile-390.png`
