# V0.3 最终验收

验收日期：2026-08-21
验收基线：`296c99b`
验收结论：PASS

## 干净克隆与环境

在 `C:\Users\ppx\AppData\Local\Temp\jil-p09-296c99b` 从 `296c99b` 创建独立克隆，并使用独立 PostgreSQL 数据库 `jil_p09_296c99b` 完成全流程。克隆中的正式题库 SHA-256 与源工作区一致：`a1f91d51d5fe1dbc687770bc3d88a0822eae4f1210327741599b5a56f301a5ec`。

| 项目 | 验收版本 |
|---|---|
| Windows | Windows 11 10.0 |
| PowerShell | 7 |
| JDK | 21.0.12.1 |
| Maven | 3.8.4 |
| Node.js | 22.13.0 |
| npm | 10.9.2 |
| Git | 2.46.1 |
| PostgreSQL | 16.15，WSL Ubuntu 24.04 |
| Browser | Playwright Chromium |

## 验收结果

| Gate | 结果 |
|---|---|
| 后端测试 | 49/49 通过 |
| 后端打包 | `mvn -B -ntp package -DskipTests` 通过，可执行 JAR 已生成 |
| 前端类型检查 | 通过 |
| Vitest | 5/5 通过 |
| 前端生产构建 | 通过 |
| Playwright Chromium | 17/17 通过，耗时 1.5 分钟 |
| Visual Regression | 11 张规定视口基线无差异 |
| Seed Import V2 | 336 题；重复导入跳过 336，warning 0 |
| Smoke | Flyway 16、题目 336、启用题目 336、场景 12、Source 3、Lab 5 |
| PostgreSQL 备份 | custom format，212991 字节；恢复库与原库计数一致 |

备份恢复使用专用临时库 `jil_p09_restore_296c99b`。原库与恢复库的 `question/scenario/content_source/lab_definition` 底表计数均为 `336/12/0/6`，验证后已删除恢复库。Source 3、Lab 5 是接口启用/聚合口径，不等同于底表总数。

## P08 性能证据

- 首页初始静态 JS：834.51 KiB / 850 KiB。
- 最大 chunk：174.26 KiB / 500 KiB。
- 首页静态依赖和浏览器请求均未加载 Lab / Source 路由模块。
- 336 题下 keyword `HashMap` 返回 7 行，执行时间 0.700 ms，shared hit 56。
- Bundle 明细：[`validation/p08/bundle-report.md`](validation/p08/bundle-report.md)
- SQL 执行计划：[`validation/p08/keyword-search-explain.md`](validation/p08/keyword-search-explain.md)

## 视觉基线

目录：`frontend/e2e/00-visual-regression.spec.ts-snapshots`

- `workbench-1440-chromium-win32.png`
- `workbench-1720-chromium-win32.png`
- `workbench-1920-chromium-win32.png`
- `workbench-mobile-390-chromium-win32.png`
- `knowledge-1720-chromium-win32.png`
- `question-1720-chromium-win32.png`
- `question-mobile-390-chromium-win32.png`
- `scenario-1720-chromium-win32.png`
- `source-1720-chromium-win32.png`
- `lab-1720-chromium-win32.png`
- `review-1720-chromium-win32.png`

## Windows 11 启停与 IDEA

五个 P09 脚本已完成实际运行验证：

- `scripts/dev-up.ps1` 启动打包后的后端 JAR 与本仓库 Vite，并保存 PID、可执行文件和命令行标记。
- `scripts/dev-down.ps1` 只停止状态文件中同时通过项目根目录、可执行文件和命令行校验的进程。
- `scripts/import-v03-bank.ps1` 执行 validate、dry-run、导入和重复幂等校验。
- `scripts/smoke-v03.ps1` 只读验证后端、前端、Flyway 和正式内容计数。
- `scripts/backup-postgres.ps1` 生成 PostgreSQL custom-format 备份；本次已实际恢复验证。

端口被占用时，启动脚本拒绝执行且不会结束占用者。启动失败时，只清理本次启动的后端和前端进程。IDEA 的 JDK、Maven、环境变量、后端 Application 配置和前端 npm 配置见根目录 `README.md`。

## 复现命令

以下命令均在 PowerShell 7、仓库根目录执行。数据库密码只放入当前进程环境变量。

```powershell
$ErrorActionPreference = 'Stop'
chcp 65001 | Out-Null
$env:JIL_JAVA_HOME = 'D:\Develop\Java\jdk-21'
$env:JIL_MAVEN_HOME = 'D:\Develop\maven\apache-maven-3.8.4'
$env:JAVA_HOME = $env:JIL_JAVA_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:JIL_MAVEN_HOME\bin;$env:Path"
$env:POSTGRES_URL = 'jdbc:postgresql://127.0.0.1:5432/java_interview_lab'
$env:POSTGRES_USER = '<本地数据库用户>'
$env:POSTGRES_PASSWORD = '<本地数据库密码>'

Push-Location .\backend
mvn -B -ntp test
mvn -B -ntp package -DskipTests
Pop-Location

Push-Location .\frontend
npm ci
npm run typecheck
npm run test
npm run performance
Pop-Location

& .\scripts\dev-up.ps1
& .\scripts\import-v03-bank.ps1
& .\scripts\smoke-v03.ps1
$env:JIL_DB_NAME = 'java_interview_lab'
$env:JIL_DB_USER = $env:POSTGRES_USER
& .\scripts\backup-postgres.ps1
& .\scripts\dev-down.ps1

Push-Location .\frontend
npm run e2e
Pop-Location
```

## 已知边界

- 模拟面试使用本地规则评分，不调用外部 LLM；这是 V0.3 的产品边界。
- 浏览器语音输入是可选增强；文本输入始终可用。
- Visual Regression 基线是 Windows Chromium，跨操作系统或浏览器不作为本次像素门禁。
- PostgreSQL 位于 WSL 时，备份脚本依赖指定发行版中存在 `pg_dump`。

全部 P00～P09 gate 为绿色，满足创建 V0.3 tag 的前置条件。本次验收未创建 tag。
