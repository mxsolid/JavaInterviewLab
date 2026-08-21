[CmdletBinding()]
param(
    [string]$DatabaseName = $env:JIL_DB_NAME,
    [string]$DatabaseUser = $(if ($env:JIL_DB_USER) { $env:JIL_DB_USER } else { $env:POSTGRES_USER }),
    [string]$DatabaseHost = $(if ($env:JIL_DB_HOST) { $env:JIL_DB_HOST } else { '127.0.0.1' }),
    [int]$DatabasePort = $(if ($env:JIL_DB_PORT) { [int]$env:JIL_DB_PORT } else { 5432 }),
    [string]$OutputDirectory = "$PSScriptRoot\..\backup",
    [string]$WslDistribution = $(if ($env:JIL_WSL_DISTRIBUTION) { $env:JIL_WSL_DISTRIBUTION } else { 'Ubuntu-24.04' })
)

$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $true
. "$PSScriptRoot\v03-common.ps1"

if (-not $DatabaseName -or -not $DatabaseUser) {
    throw '请设置 JIL_DB_NAME 与 JIL_DB_USER，或传入 DatabaseName 与 DatabaseUser。'
}
if (-not $env:PGPASSWORD -and $env:POSTGRES_PASSWORD) {
    $env:PGPASSWORD = $env:POSTGRES_PASSWORD
}

$resolvedOutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Path $resolvedOutputDirectory -Force | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$outputPath = Join-Path $resolvedOutputDirectory "${DatabaseName}_$timestamp.dump"

$nativePgDump = $null
if ($env:POSTGRES_BIN) {
    $candidate = Join-Path $env:POSTGRES_BIN 'pg_dump.exe'
    if (Test-Path -LiteralPath $candidate -PathType Leaf) { $nativePgDump = $candidate }
}
if (-not $nativePgDump) {
    $command = Get-Command pg_dump -ErrorAction SilentlyContinue
    if ($command) { $nativePgDump = $command.Source }
}

if ($nativePgDump) {
    & $nativePgDump -h $DatabaseHost -p $DatabasePort -U $DatabaseUser -F c -d $DatabaseName -f $outputPath
} else {
    $wslCommand = (Get-Command wsl -ErrorAction SilentlyContinue).Source
    if (-not $wslCommand) {
        throw '未找到 pg_dump；请设置 POSTGRES_BIN，或安装带 /usr/bin/pg_dump 的 WSL 发行版。'
    }

    $previousWslEnv = $env:WSLENV
    try {
        $wslNames = @($previousWslEnv -split ':' | Where-Object { $_ })
        if ('PGPASSWORD' -notin $wslNames) {
            $env:WSLENV = (@('PGPASSWORD') + $wslNames) -join ':'
        }
        $wslOutputPath = (& $wslCommand -d $WslDistribution -e /usr/bin/wslpath -a -u $outputPath).Trim()
        & $wslCommand -d $WslDistribution -e /usr/bin/pg_dump `
            -h $DatabaseHost -p $DatabasePort -U $DatabaseUser -F c -d $DatabaseName -f $wslOutputPath
    }
    finally {
        $env:WSLENV = $previousWslEnv
    }
}

$backupFile = Get-Item -LiteralPath $outputPath -ErrorAction Stop
if ($backupFile.Length -le 0) {
    throw "备份文件为空：$outputPath"
}

[pscustomobject]@{
    Database = $DatabaseName
    CreatedAt = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
    Format = 'PostgreSQL custom'
    Bytes = $backupFile.Length
    Path = $backupFile.FullName
} | Format-List
