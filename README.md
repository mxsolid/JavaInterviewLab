# Java Interview Lab

Java 后端面试学习系统。PostgreSQL 是题库与学习数据的唯一事实源。

当前版本：V0.3。包含 336 题正式题库、学习闭环、场景训练、源码注释、算法实验和本地可解释模拟面试。

## 环境

- Windows 11、PowerShell 7：`D:\Tools\PowerShell\7\pwsh.exe`
- JDK 21：`D:\Develop\Java\jdk-21`
- Maven 3.8.4：`D:\Develop\maven\apache-maven-3.8.4`
- Node.js 22.13.0、npm 10.9.2
- PostgreSQL 16+
- Chrome，用于 Playwright Chromium 验收

所有命令均在 PowerShell 7 中执行。密码只放在当前进程环境变量或本机密码管理中，不写入 Git。

## Fresh clone

### 1. clone 与配置

```powershell
$ErrorActionPreference = 'Stop'
chcp 65001
git clone <repository-url> JavaInterviewLab
Set-Location .\JavaInterviewLab
Copy-Item .\backend\src\main\resources\application-local.yml.example `
  .\backend\src\main\resources\application-local.yml

$env:JAVA_HOME = 'D:\Develop\Java\jdk-21'
$env:JIL_JAVA_HOME = $env:JAVA_HOME
$env:JIL_MAVEN_HOME = 'D:\Develop\maven\apache-maven-3.8.4'
$env:Path = "$env:JAVA_HOME\bin;$env:JIL_MAVEN_HOME\bin;$env:Path"
$env:POSTGRES_URL = 'jdbc:postgresql://127.0.0.1:5432/java_interview_lab'
$env:POSTGRES_USER = '<本地数据库用户>'
$env:POSTGRES_PASSWORD = '<本地数据库密码>'
```

### 2. 创建数据库

Windows PostgreSQL 工具在 PATH 中时：

```powershell
$env:PGPASSWORD = $env:POSTGRES_PASSWORD
createdb -h 127.0.0.1 -p 5432 -U $env:POSTGRES_USER java_interview_lab
```

PostgreSQL 位于 WSL 时：

```powershell
$env:PGPASSWORD = $env:POSTGRES_PASSWORD
$env:WSLENV = 'PGPASSWORD'
wsl -d Ubuntu-24.04 -e /usr/bin/createdb `
  -h 127.0.0.1 -p 5432 -U $env:POSTGRES_USER java_interview_lab
```

若数据库已存在，跳过创建。Flyway 只负责库内 schema，不创建 PostgreSQL database。

### 3. 后端测试与打包

```powershell
Push-Location .\backend
mvn -B -ntp test
mvn -B -ntp package -DskipTests
Pop-Location
```

### 4. 前端安装、测试与构建

```powershell
Push-Location .\frontend
npm ci
npm run typecheck
npm run test
npm run performance
Pop-Location
```

### 5. 启动、导入、Smoke、备份

```powershell
& .\scripts\dev-up.ps1
& .\scripts\import-v03-bank.ps1
& .\scripts\smoke-v03.ps1

$env:JIL_DB_NAME = 'java_interview_lab'
$env:JIL_DB_USER = $env:POSTGRES_USER
& .\scripts\backup-postgres.ps1
```

- 前端：`http://127.0.0.1:5173`
- Swagger UI：`http://127.0.0.1:8080/swagger-ui.html`
- OpenAPI：`http://127.0.0.1:8080/v3/api-docs`

`dev-up.ps1` 只启动打包后的后端 JAR 和本仓库 Vite。PID、可执行文件和项目命令行标记写入 `.runtime/dev-processes.json`。端口已占用时脚本直接失败，不结束占用进程。

停止脚本只处理状态文件中同时通过可执行文件与命令行校验的两个 PID：

```powershell
& .\scripts\dev-down.ps1
```

### 6. 浏览器 E2E

先执行 `dev-down.ps1` 释放 8080/5173。Playwright 会重建独立 `jil_e2e` 数据库并托管后端、前端：

```powershell
Push-Location .\frontend
npm run e2e
Pop-Location
```

E2E 不复用本地学习历史，也不删除 `java_interview_lab` 或 `devdb`。

## IntelliJ IDEA

后端配置：

1. Project SDK 和 backend Module SDK 选择 JDK 21。
2. Maven home 选择 `D:\Develop\maven\apache-maven-3.8.4`。
3. 新建 Application 配置，Main class 为 `com.javainterviewlab.JavaInterviewLabApplication`，Use classpath of module 选择 backend。
4. Environment variables 设置 `POSTGRES_URL`、`POSTGRES_USER`、`POSTGRES_PASSWORD`；Active profile 使用 `local`。
5. 启动后检查 `/actuator/health` 返回 `UP`。

前端配置：

1. Node interpreter 选择 Node 22.13.0。
2. 新建 npm 配置，package.json 选择 `frontend/package.json`，Command 为 `run`，Scripts 为 `dev`。
3. 后端就绪后启动，访问 `http://127.0.0.1:5173`。

IDEA 启动的进程应使用 IDEA Stop 按钮停止。`dev-down.ps1` 只管理 `dev-up.ps1` 写入 PID 文件的进程。

## 常用脚本

| 脚本 | 作用 |
|---|---|
| `scripts/dev-up.ps1` | 精确启动打包后端与 Vite，保存 PID 和日志 |
| `scripts/dev-down.ps1` | 校验 PID、可执行文件、命令行后停止本项目进程 |
| `scripts/smoke-v03.ps1` | 只读检查健康、Flyway、正式内容和前端入口 |
| `scripts/import-v03-bank.ps1` | validate、dry-run、真实导入和重复幂等校验 |
| `scripts/backup-postgres.ps1` | 使用原生或 WSL `pg_dump` 生成 custom-format 备份 |

备份文件默认进入被 Git 忽略的 `backup/`。运行日志位于被 Git 忽略的 `.runtime/logs/`。

## 设计与验收

- V0.3 最终验收：`docs/v03/V03_FINAL_ACCEPTANCE.md`
- Seed Import V2：`docs/v03/02_seed_import_v2.md`
- P08 E2E / Visual / Performance：`docs/v03/validation/p08/validation.md`
