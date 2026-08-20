param(
    [string]$BaseUrl = "http://127.0.0.1:8080"
)

$ErrorActionPreference = 'Stop'
chcp 65001 | Out-Null
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $OutputEncoding
[Console]::OutputEncoding = $OutputEncoding

function Get-ApiData([string]$Path) {
    $response = Invoke-RestMethod -Uri "$BaseUrl$Path" -Method Get
    if (-not $response.success) {
        throw "接口失败: $Path, code=$($response.code), traceId=$($response.traceId)"
    }
    return $response.data
}

# 此脚本只读取健康检查和已有学习数据；重启前后运行两次可比较数据是否仍存在，不删除任何内容。
$health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get
if ($health.status -ne 'UP') {
    throw "后端未就绪: $($health.status)"
}

$dashboard = Get-ApiData '/api/dashboard'
$wrongQuestions = Get-ApiData '/api/study/wrong-questions'
$favorites = Get-ApiData '/api/study/favorites'
$reviews = Get-ApiData '/api/study/reviews?status=PENDING'

[pscustomobject]@{
    CheckedAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
    TouchedQuestionCount = $dashboard.touchedQuestionCount
    ActiveWrongQuestionCount = $wrongQuestions.Count
    FavoriteQuestionCount = $favorites.Count
    PendingReviewCount = $reviews.Count
    RecentStudyItemCount = $dashboard.recentStudyItems.Count
} | Format-List
