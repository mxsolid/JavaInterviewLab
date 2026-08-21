[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
. "$PSScriptRoot\v03-common.ps1"

if (-not (Test-Path -LiteralPath $script:JilStatePath -PathType Leaf)) {
    Write-Output '未发现本项目运行状态文件；未结束任何进程。'
    return
}

$state = Get-Content -LiteralPath $script:JilStatePath -Raw -Encoding UTF8 | ConvertFrom-Json
$stateProjectRoot = [System.IO.Path]::GetFullPath([string]$state.projectRoot)
if (-not $stateProjectRoot.Equals($script:JilProjectRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "状态文件项目路径不匹配，拒绝停止进程：$stateProjectRoot"
}

# 每个 PID 都必须同时匹配可执行文件和项目路径标记，绝不按 java/node 名称批量结束。
Stop-JilManagedProcess $state.frontend
Stop-JilManagedProcess $state.backend
Remove-Item -LiteralPath $script:JilStatePath -Force
Write-Output '本项目后端与前端进程已停止。运行日志保留在 .runtime/logs。'
