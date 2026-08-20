. "$PSScriptRoot\00_set-utf8.ps1"

# 模板脚本：V0.2 数据稳定后再正式启用。
# 不在仓库内保存数据库密码。
# 推荐通过环境变量 PGPASSWORD 或 pgpass 管理认证。

$database = $env:JIL_DB_NAME
$user = $env:JIL_DB_USER
$hostName = if ($env:JIL_DB_HOST) { $env:JIL_DB_HOST } else { "localhost" }
$port = if ($env:JIL_DB_PORT) { $env:JIL_DB_PORT } else { "5432" }

if (-not $database -or -not $user) {
    Write-Error "请设置 JIL_DB_NAME 和 JIL_DB_USER。"
    exit 1
}

$pgDump = Get-Command pg_dump -ErrorAction SilentlyContinue
if (-not $pgDump) {
    Write-Error "PATH 中未找到 pg_dump。请配置 PostgreSQL bin 目录。"
    exit 1
}

$backupDir = Join-Path $PSScriptRoot "..\backup"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outFile = Join-Path $backupDir "java_interview_lab_$timestamp.dump"

& $pgDump.Source -h $hostName -p $port -U $user -F c -d $database -f $outFile

if ($LASTEXITCODE -eq 0) {
    Write-Host "备份完成：$outFile" -ForegroundColor Green
} else {
    Write-Error "备份失败，退出码：$LASTEXITCODE"
}
