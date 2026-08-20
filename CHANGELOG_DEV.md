# 开发变更记录

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
