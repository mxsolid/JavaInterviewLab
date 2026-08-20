# Java Interview Lab PowerShell UTF-8 初始化脚本
# 目的：避免 Windows 控制台、中文 Markdown、SQL、JSON 在 Agent 修改时出现乱码。

chcp 65001 | Out-Null

$utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8

Write-Host "PowerShell UTF-8 环境已初始化。" -ForegroundColor Green
Write-Host "InputEncoding : $([Console]::InputEncoding.WebName)"
Write-Host "OutputEncoding: $([Console]::OutputEncoding.WebName)"
