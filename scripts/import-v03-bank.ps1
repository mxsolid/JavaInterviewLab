[CmdletBinding()]
param(
    [string]$ApiBase = 'http://127.0.0.1:8080',
    [string]$BankPath = "$PSScriptRoot\..\backend\src\main\resources\seed\v03-core-complete.json"
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
. "$PSScriptRoot\v03-common.ps1"

$resolvedBankPath = (Resolve-Path -LiteralPath $BankPath -ErrorAction Stop).Path
$bankFile = Get-Item -LiteralPath $resolvedBankPath -ErrorAction Stop
if ($bankFile.Extension -ne '.json') {
    throw 'BankPath 必须指向 JSON 文件。'
}

function Invoke-JilSeedPost([string]$Path) {
    $response = Invoke-RestMethod -Method Post -Uri "$ApiBase$Path" -Form @{ file = $bankFile } -TimeoutSec 60
    if (-not $response.success) {
        throw "Seed 接口失败：$Path，code=$($response.code)，traceId=$($response.traceId)"
    }
    return $response.data
}

$validation = Invoke-JilSeedPost '/api/v1/system/seeds/validate'
if (-not $validation.valid -or $validation.questionCount -ne 336) {
    throw "V0.3 Seed 校验结果不正确：valid=$($validation.valid)，questions=$($validation.questionCount)"
}

$dryRun = Invoke-JilSeedPost '/api/v1/system/seeds/import?dryRun=true'
$import = Invoke-JilSeedPost '/api/v1/system/seeds/import?dryRun=false'
$repeat = Invoke-JilSeedPost '/api/v1/system/seeds/import?dryRun=false'

$checksums = @($validation.checksumSha256, $dryRun.checksumSha256, $import.checksumSha256, $repeat.checksumSha256) |
    Select-Object -Unique
if ($checksums.Count -ne 1) {
    throw 'validate、dry-run、import 的 checksum 不一致。'
}
if ($repeat.created -ne 0 -or $repeat.updated -ne 0 -or $repeat.skipped -ne 336) {
    throw '相同版本重复导入没有保持 336 题幂等。'
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
