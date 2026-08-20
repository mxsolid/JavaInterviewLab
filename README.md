# Java Interview Lab

面向 Java 后端面试学习的本地系统。

当前仅完成 A01 项目初始化。后续任务严格按 `PROGRESS.md` 的顺序独立执行。

## 技术栈

- Java 21、Spring Boot 3.5.x、Spring MVC、MyBatis、Flyway
- PostgreSQL 16+
- React 19、TypeScript、Vite 8、Ant Design、TanStack Query

V0.1～V0.3 不依赖 Redis。学习进度、答题历史、笔记和错题均以 PostgreSQL 为唯一事实源。

## 目录

```text
backend/     后端，A02 开始创建
frontend/    前端，A03 开始创建
database/    数据库草案与后续迁移资料
docs/        产品、架构、数据模型与验收文档
prompts/     单任务开发提示模板
reference/   UI 原型
scripts/     Windows 本地开发脚本
seeds/       初始化内容样例
```

## 开发约束

开始任务前先阅读 `AGENTS.md`、`PROGRESS.md` 和当前任务对应文档。每次只处理一个 Task ID，并在完成时同步更新 `PROGRESS.md` 与 `CHANGELOG_DEV.md`。
