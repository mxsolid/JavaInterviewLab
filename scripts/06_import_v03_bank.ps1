[CmdletBinding()]
param(
    [string]$ApiBase = "http://127.0.0.1:8080",
    [string]$BankPath = "$PSScriptRoot\..\backend\src\main\resources\seed\v03-core-complete.json"
)

$ErrorActionPreference = 'Stop'
chcp 65001 | Out-Null
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $OutputEncoding
[Console]::OutputEncoding = $OutputEncoding

$resolvedBankPath = (Resolve-Path -LiteralPath $BankPath -ErrorAction Stop).Path
$bankFile = Get-Item -LiteralPath $resolvedBankPath -ErrorAction Stop
if ($bankFile.Extension -ne '.json') {
    throw 'BankPath 必须指向 JSON 文件。'
}

function Invoke-SeedPost([string]$Path) {
    $response = Invoke-RestMethod -Method Post -Uri "$ApiBase$Path" -Form @{ file = $bankFile }
    if (-not $response.success) {
        throw "Seed 接口失败: $Path, code=$($response.code), traceId=$($response.traceId)"
    }
    return $response.data
}

$validation = Invoke-SeedPost '/api/v1/system/seeds/validate'
$dryRun = Invoke-SeedPost '/api/v1/system/seeds/import?dryRun=true'
$import = Invoke-SeedPost '/api/v1/system/seeds/import?dryRun=false'
$repeat = Invoke-SeedPost '/api/v1/system/seeds/import?dryRun=false'

$checksumMismatch = $validation.checksumSha256 -ne $dryRun.checksumSha256 `
    -or $validation.checksumSha256 -ne $import.checksumSha256 `
    -or $validation.checksumSha256 -ne $repeat.checksumSha256
if ($checksumMismatch) {
    throw 'validate、dry-run、import 的 checksum 不一致。'
}
if ($repeat.created -ne 0 -or $repeat.updated -ne 0) {
    throw '相同版本重复导入发生了 mutation。'
}

[pscustomobject]@{
    SeedPack = $import.seedPack
    Version = $import.version
    Checksum = $import.checksumSha256
    Questions = $import.questionCount
    Created = $import.created
    Updated = $import.updated
    SkippedOnRepeat = $repeat.skipped
    WarningCount = @($import.warnings).Count
} | Format-List
