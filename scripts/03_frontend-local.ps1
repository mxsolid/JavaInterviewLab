. "$PSScriptRoot\00_set-utf8.ps1"

$projectRoot = "D:\Develop\project\Ai\JavaInterviewLab"
Set-Location "$projectRoot\frontend"

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "未找到 npm，请先安装/配置 Node.js。"
    exit 1
}

npm run dev
