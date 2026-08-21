[CmdletBinding()]
param(
    [string]$BackendBaseUrl = 'http://127.0.0.1:8080',
    [string]$FrontendBaseUrl = 'http://127.0.0.1:5173'
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
. "$PSScriptRoot\v03-common.ps1"

function Get-JilApiData([string]$Path) {
    $response = Invoke-RestMethod -Uri "$BackendBaseUrl$Path" -Method Get -TimeoutSec 10
    if (-not $response.success) {
        throw "接口失败：$Path，code=$($response.code)，traceId=$($response.traceId)"
    }
    return $response.data
}

$health = Invoke-RestMethod -Uri "$BackendBaseUrl/actuator/health" -Method Get -TimeoutSec 10
if ($health.status -ne 'UP') { throw "后端健康状态不是 UP：$($health.status)" }

$status = Get-JilApiData '/api/v1/system/status'
if ($status.status -ne 'UP') { throw "系统状态不是 UP：$($status.status)" }
if ([int]$status.flywayVersion -lt 16) { throw "Flyway 版本不足：$($status.flywayVersion)" }
if ([long]$status.enabledQuestionCount -lt 336) { throw "启用题目不足 336：$($status.enabledQuestionCount)" }
if ([long]$status.scenarioCount -ne 12) { throw "场景数量不是 12：$($status.scenarioCount)" }
if ([long]$status.sourceSnippetCount -ne 3) { throw "Source 数量不是 3：$($status.sourceSnippetCount)" }
if ([long]$status.labCount -ne 5) { throw "Lab 定义数量不是 5：$($status.labCount)" }

$null = Get-JilApiData '/api/v1/workbench'
$null = Get-JilApiData '/api/v1/knowledge-map'
$null = Get-JilApiData '/api/v1/scenarios'
$null = Get-JilApiData '/api/v1/source-snippets'
$null = Get-JilApiData '/api/v1/labs'

$frontend = Invoke-WebRequest -Uri $FrontendBaseUrl -Method Get -TimeoutSec 10
if ($frontend.StatusCode -ne 200 -or -not $frontend.Content.Contains('<div id="root">')) {
    throw '前端入口未返回可挂载的 React 页面。'
}

[pscustomobject]@{
    CheckedAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
    Backend = $health.status
    FrontendStatus = $frontend.StatusCode
    Flyway = $status.flywayVersion
    Questions = $status.questionCount
    EnabledQuestions = $status.enabledQuestionCount
    Scenarios = $status.scenarioCount
    Sources = $status.sourceSnippetCount
    Labs = $status.labCount
} | Format-List
