[CmdletBinding()]
param(
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5173
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
. "$PSScriptRoot\v03-common.ps1"

if (Test-Path -LiteralPath $script:JilStatePath) {
    throw "检测到运行状态文件，请先执行 scripts/dev-down.ps1：$script:JilStatePath"
}

Assert-JilPortAvailable $BackendPort
Assert-JilPortAvailable $FrontendPort

$javaHomePath = if ($env:JIL_JAVA_HOME) { $env:JIL_JAVA_HOME } else { 'D:\Develop\Java\jdk-21' }
$javaCommand = Join-Path $javaHomePath 'bin\java.exe'
$backendJar = Join-Path $script:JilProjectRoot 'backend\target\java-interview-lab-backend-0.1.0-SNAPSHOT.jar'
$viteEntry = Join-Path $script:JilProjectRoot 'frontend\node_modules\vite\bin\vite.js'
$nodeCommand = if ($env:JIL_NODE_HOME) {
    Join-Path $env:JIL_NODE_HOME 'node.exe'
} else {
    (Get-Command node -ErrorAction Stop).Source
}

foreach ($requiredPath in @($javaCommand, $backendJar, $nodeCommand, $viteEntry)) {
    if (-not (Test-Path -LiteralPath $requiredPath -PathType Leaf)) {
        throw "启动依赖不存在：$requiredPath"
    }
}

New-Item -ItemType Directory -Path $script:JilRuntimeDirectory -Force | Out-Null
$logDirectory = Join-Path $script:JilRuntimeDirectory 'logs'
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backendOut = Join-Path $logDirectory "backend-$timestamp.out.log"
$backendErr = Join-Path $logDirectory "backend-$timestamp.err.log"
$frontendOut = Join-Path $logDirectory "frontend-$timestamp.out.log"
$frontendErr = Join-Path $logDirectory "frontend-$timestamp.err.log"

$backendProcess = $null
$frontendProcess = $null
try {
    $backendProcess = Start-Process -FilePath $javaCommand `
        -ArgumentList "-jar `"$backendJar`" --spring.profiles.active=local --server.port=$BackendPort" `
        -WorkingDirectory (Join-Path $script:JilProjectRoot 'backend') `
        -RedirectStandardOutput $backendOut `
        -RedirectStandardError $backendErr `
        -WindowStyle Hidden `
        -PassThru

    $frontendProcess = Start-Process -FilePath $nodeCommand `
        -ArgumentList "`"$viteEntry`" --host 127.0.0.1 --port $FrontendPort --strictPort" `
        -WorkingDirectory (Join-Path $script:JilProjectRoot 'frontend') `
        -RedirectStandardOutput $frontendOut `
        -RedirectStandardError $frontendErr `
        -WindowStyle Hidden `
        -PassThru

    $state = [ordered]@{
        projectRoot = $script:JilProjectRoot
        startedAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
        backend = [ordered]@{
            pid = $backendProcess.Id
            executable = $javaCommand
            commandMarker = $backendJar
            stdout = $backendOut
            stderr = $backendErr
        }
        frontend = [ordered]@{
            pid = $frontendProcess.Id
            executable = $nodeCommand
            commandMarker = $viteEntry
            stdout = $frontendOut
            stderr = $frontendErr
        }
    }
    $state | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $script:JilStatePath -Encoding UTF8

    Wait-JilHttpEndpoint "http://127.0.0.1:$BackendPort/actuator/health" $backendProcess 90
    Wait-JilHttpEndpoint "http://127.0.0.1:$FrontendPort" $frontendProcess 30
}
catch {
    if ($frontendProcess -and -not $frontendProcess.HasExited) { Stop-Process -Id $frontendProcess.Id -Force }
    if ($backendProcess -and -not $backendProcess.HasExited) { Stop-Process -Id $backendProcess.Id -Force }
    if (Test-Path -LiteralPath $script:JilStatePath) { Remove-Item -LiteralPath $script:JilStatePath -Force }
    throw
}

[pscustomobject]@{
    Backend = "http://127.0.0.1:$BackendPort"
    Frontend = "http://127.0.0.1:$FrontendPort"
    BackendPid = $backendProcess.Id
    FrontendPid = $frontendProcess.Id
    StateFile = $script:JilStatePath
} | Format-List
