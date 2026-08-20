. "$PSScriptRoot\00_set-utf8.ps1"

$javaHome = "D:\Develop\Java\jdk-21"
$mavenRoot = "D:\Develop\maven"
$projectRoot = "D:\Develop\project\Ai\JavaInterviewLab"

Write-Host "`n=== Java ===" -ForegroundColor Cyan
if (Test-Path "$javaHome\bin\java.exe") {
    & "$javaHome\bin\java.exe" -version
} else {
    Write-Error "未找到 JDK 21：$javaHome"
}

Write-Host "`n=== Maven 候选目录 ===" -ForegroundColor Cyan
if (Test-Path $mavenRoot) {
    Get-ChildItem -Path $mavenRoot -Directory | ForEach-Object {
        $mvn = Join-Path $_.FullName "bin\mvn.cmd"
        if (Test-Path $mvn) {
            Write-Host "`nMaven: $($_.FullName)" -ForegroundColor Yellow
            & $mvn -version
        }
    }
} else {
    Write-Error "未找到 Maven 根目录：$mavenRoot"
}

Write-Host "`n=== 项目目录 ===" -ForegroundColor Cyan
if (Test-Path $projectRoot) {
    Write-Host "项目目录存在：$projectRoot" -ForegroundColor Green
} else {
    Write-Warning "项目目录尚不存在或未初始化：$projectRoot"
}

Write-Host "`n=== PostgreSQL ===" -ForegroundColor Cyan
$psql = Get-Command psql -ErrorAction SilentlyContinue
if ($psql) {
    & $psql.Source --version
} else {
    Write-Warning "PATH 中未找到 psql。若 PostgreSQL 已安装，可后续在 IDEA 中配置或设置 POSTGRES_BIN。"
}
