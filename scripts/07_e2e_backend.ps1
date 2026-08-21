$ErrorActionPreference = 'Stop'
chcp 65001 | Out-Null
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $OutputEncoding
[Console]::OutputEncoding = $OutputEncoding

$javaHomePath = if ($env:JIL_JAVA_HOME) { $env:JIL_JAVA_HOME } else { 'D:\Develop\Java\jdk-21' }
$mavenHomePath = if ($env:JIL_MAVEN_HOME) { $env:JIL_MAVEN_HOME } else { 'D:\Develop\maven\apache-maven-3.8.4' }
$mavenCommand = Join-Path $mavenHomePath 'bin\mvn.cmd'
$localConfigPath = Join-Path $PSScriptRoot '..\backend\src\main\resources\application-local.yml'

if (-not (Test-Path -LiteralPath $mavenCommand)) {
    throw "Maven 不存在：$mavenCommand"
}

function Resolve-PostgresSetting([string]$environmentName, [string]$pattern) {
    $environmentValue = [Environment]::GetEnvironmentVariable($environmentName)
    if ($environmentValue) {
        return $environmentValue
    }
    if (-not (Test-Path -LiteralPath $localConfigPath)) {
        throw "缺少 $environmentName，且本地配置不存在：$localConfigPath"
    }
    $configText = Get-Content -LiteralPath $localConfigPath -Raw -Encoding UTF8
    $match = [regex]::Match($configText, $pattern)
    if (-not $match.Success) {
        throw "无法从本地配置解析 $environmentName"
    }
    return $match.Groups[1].Value
}

$postgresUrl = Resolve-PostgresSetting 'POSTGRES_URL' 'url:\s*\$\{POSTGRES_URL:([^}]+)\}'
$postgresUser = Resolve-PostgresSetting 'POSTGRES_USER' 'username:\s*\$\{POSTGRES_USER:([^}]+)\}'
$postgresPassword = Resolve-PostgresSetting 'POSTGRES_PASSWORD' 'password:\s*\$\{POSTGRES_PASSWORD:([^}]+)\}'
$e2eDatabaseUrl = $postgresUrl -replace '/[^/?]+(\?.*)?$', '/jil_e2e$1'
$postgresDriver = Get-ChildItem -LiteralPath (Join-Path $env:USERPROFILE '.m2\repository\org\postgresql\postgresql') -Filter 'postgresql-*.jar' -File -Recurse |
    Sort-Object -Property LastWriteTimeUtc -Descending |
    Select-Object -First 1
if (-not $postgresDriver) {
    throw '本地 Maven 仓库缺少 PostgreSQL JDBC 驱动'
}

$env:JAVA_HOME = $javaHomePath
$env:Path = "$(Join-Path $javaHomePath 'bin');$env:Path"
$env:E2E_DB_ADMIN_URL = $postgresUrl
$env:E2E_DB_USER = $postgresUser
$env:E2E_DB_PASSWORD = $postgresPassword
& (Join-Path $javaHomePath 'bin\java.exe') --class-path $postgresDriver.FullName (Join-Path $PSScriptRoot 'PrepareE2eDatabase.java')
if ($LASTEXITCODE -ne 0) {
    throw "E2E 数据库准备失败，exitCode=$LASTEXITCODE"
}

$env:SPRING_PROFILES_ACTIVE = 'local,e2e'
$env:SPRING_DATASOURCE_URL = $e2eDatabaseUrl
$env:SPRING_DATASOURCE_USERNAME = $postgresUser
$env:SPRING_DATASOURCE_PASSWORD = $postgresPassword

Push-Location (Join-Path $PSScriptRoot '..\backend')
try {
    & $mavenCommand -B -ntp spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "E2E 后端启动失败，exitCode=$LASTEXITCODE"
    }
}
finally {
    Pop-Location
}
