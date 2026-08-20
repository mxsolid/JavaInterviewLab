# 开发变更记录

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
