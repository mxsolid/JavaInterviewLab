$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
chcp 65001 | Out-Null
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $OutputEncoding
[Console]::OutputEncoding = $OutputEncoding

$script:JilProjectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$script:JilRuntimeDirectory = Join-Path $script:JilProjectRoot '.runtime'
$script:JilStatePath = Join-Path $script:JilRuntimeDirectory 'dev-processes.json'

function Assert-JilPortAvailable([int]$Port) {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
    try {
        $listener.Start()
    }
    catch [System.Net.Sockets.SocketException] {
        throw "端口 $Port 已被占用；为避免影响无关进程，脚本不会自动结束占用者。"
    }
    finally {
        $listener.Stop()
    }
}

function Wait-JilHttpEndpoint(
    [string]$Uri,
    [System.Diagnostics.Process]$Process,
    [int]$TimeoutSeconds
) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ($Process.HasExited) {
            throw "进程在接口就绪前退出：$Uri，exitCode=$($Process.ExitCode)"
        }
        try {
            $response = Invoke-WebRequest -Uri $Uri -Method Get -TimeoutSec 3
            if ($response.StatusCode -eq 200) {
                return
            }
        }
        catch {
            Start-Sleep -Milliseconds 500
        }
    }
    throw "等待接口超时：$Uri"
}

function Stop-JilManagedProcess([pscustomobject]$Record) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($Record.pid)" -ErrorAction SilentlyContinue
    if ($null -eq $process) {
        return
    }

    $actualExecutable = [System.IO.Path]::GetFullPath($process.ExecutablePath)
    $expectedExecutable = [System.IO.Path]::GetFullPath([string]$Record.executable)
    if (-not $actualExecutable.Equals($expectedExecutable, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "PID $($Record.pid) 的可执行文件不匹配，拒绝停止：$actualExecutable"
    }
    if (-not $process.CommandLine.Contains([string]$Record.commandMarker, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "PID $($Record.pid) 的命令行不属于当前项目，拒绝停止。"
    }

    Stop-Process -Id $Record.pid -ErrorAction Stop
    try {
        Wait-Process -Id $Record.pid -Timeout 10 -ErrorAction Stop
    }
    catch {
        $remaining = Get-Process -Id $Record.pid -ErrorAction SilentlyContinue
        if ($remaining) {
            Stop-Process -Id $Record.pid -Force -ErrorAction Stop
            Wait-Process -Id $Record.pid -Timeout 5 -ErrorAction Stop
        }
    }
}
