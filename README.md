# Java Interview Lab

Java 后端面试学习系统。本地运行，题库内容和后续学习数据以 PostgreSQL 为唯一事实源。

当前版本：V0.1。已具备分类、专题、标签、题库编辑、分层答案、追问、搜索筛选、英文发音和 JSON Seed 导入。下一阶段为 V0.2 学习闭环，尚未开始。

## 技术栈

- Java 21、Spring Boot 3.5.x、Spring MVC、MyBatis XML、Flyway、PostgreSQL 16+
- React 19、TypeScript、Vite 8、Ant Design、TanStack Query

## 目录

```text
backend/     Spring Boot 服务与 Flyway migration
frontend/    React 页面和 API 客户端
docs/        产品、架构、接口与运行说明
seeds/       可上传导入的 JSON 题库包
```

## PostgreSQL 与后端

复制 `backend/src/main/resources/application-local.yml.example` 为 `application-local.yml`，然后设置 `POSTGRES_PASSWORD`。Flyway 会自动执行未应用 migration。

```powershell
chcp 65001
$env:JAVA_HOME = "D:\Develop\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;D:\Develop\maven\apache-maven-3.8.4\bin;$env:Path"
$env:POSTGRES_PASSWORD = "<本地密码>"
Set-Location backend
mvn spring-boot:run
```

Swagger UI：`http://127.0.0.1:8080/swagger-ui.html`。OpenAPI JSON：`http://127.0.0.1:8080/v3/api-docs`。

## 前端

```powershell
chcp 65001
$env:Path = "D:\Develop\node\nvm\v22.13.0;$env:Path"
Set-Location frontend
npm ci
npm run typecheck
npm run build
npm run dev
```

开发服务器通过 Vite 代理请求 `/api`；可用 `VITE_BACKEND_URL` 覆盖默认后端地址。

## Seed 与测试

在管理页选择 `seeds/v01-core.json` 上传导入。接口为 `POST /api/system/seeds/import`，表单字段名为 `file`。`seedPack`、`version` 和题目 `externalKey` 保证重复导入不产生重复题目。

后端测试：`mvn -B -ntp test`。IDEA 可直接运行 `JavaInterviewLabApplication`，或运行 `backend/src/test` 下的测试类。
