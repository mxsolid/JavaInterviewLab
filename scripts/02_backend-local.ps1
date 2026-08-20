. "$PSScriptRoot\00_set-utf8.ps1"

$projectRoot = "D:\Develop\project\Ai\JavaInterviewLab"
$javaHome = "D:\Develop\Java\jdk-21"

$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Set-Location "$projectRoot\backend"

Write-Host "启动后端 local profile..." -ForegroundColor Cyan
Write-Host "注意：数据库连接信息应从环境变量或 application-local.yml 的非敏感配置读取。" -ForegroundColor Yellow

# Maven 路径由实际环境确认后再设置；不要在脚本里猜测具体 Maven 子目录。
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
