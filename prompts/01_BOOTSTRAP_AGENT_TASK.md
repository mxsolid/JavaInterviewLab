# Agent Task：A01～A04 地基任务执行模板

一次只替换并执行一个 Task ID。

## A01
初始化项目目录、Git 基础文件、`.editorconfig`、`.gitignore`、README、PROGRESS、CHANGELOG_DEV。
不要创建业务代码。

## A02
创建 backend：
- Java 21
- Spring Boot 3.5.x
- Maven
- Spring Web
- Validation
- Actuator
- MyBatis
- PostgreSQL Driver
- Flyway
- Test

创建：
- 主启动类
- local profile
- 统一 API Response
- 全局异常
- traceId
- `/actuator/health`

## A03
创建 frontend：
- React 19
- TypeScript
- Vite 8
- Router
- Ant Design
- TanStack Query
- 白色教育产品主题

只完成：
- AppShell
- 首页空骨架
- Loading / Error / Empty
- API client

## A04
创建 PostgreSQL baseline：
- Flyway V1
- profile
- category
- topic
- question
- question_answer
- question_follow_up
- tag
- question_tag
- content_source
- content_relation

确保从全新数据库可一次迁移成功。
