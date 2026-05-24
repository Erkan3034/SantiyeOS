$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent (Split-Path -Parent $PSCommandPath)
$javaRoot = Join-Path $projectRoot "src\main\java"
$resourceRoot = Join-Path $projectRoot "src\main\resources"

$failed = $false

function Fail-Check {
    param([string] $Message)
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    $script:failed = $true
}

function Pass-Check {
    param([string] $Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

$rawSqlPatterns = @(
    "SELECT\s+.*\s+FROM",
    "INSERT\s+INTO",
    "UPDATE\s+\w+\s+SET",
    "DELETE\s+FROM",
    "createStatement\(",
    "prepareStatement\("
)

$sqlFindings = Get-ChildItem -Path $javaRoot -Recurse -Filter *.java |
    Select-String -Pattern $rawSqlPatterns -CaseSensitive:$false

if ($sqlFindings) {
    $sqlFindings | ForEach-Object {
        Write-Host "$($_.Path):$($_.LineNumber) $($_.Line.Trim())" -ForegroundColor Yellow
    }
    Fail-Check "Java kodunda raw SQL benzeri ifade bulundu. DAL sadece stored procedure cagrisi yapmali."
} else {
    Pass-Check "Java kodunda raw SQL bulunmadi."
}

$secretPatterns = @(
    "password\s*=\s*[^$]",
    "secret\s*=\s*[^$]",
    "api[-_]?key\s*=\s*[^$]"
)

$secretFindings = Get-ChildItem -Path $resourceRoot -Recurse -File |
    Select-String -Pattern $secretPatterns -CaseSensitive:$false

if ($secretFindings) {
    $secretFindings | ForEach-Object {
        Write-Host "$($_.Path):$($_.LineNumber) $($_.Line.Trim())" -ForegroundColor Yellow
    }
    Fail-Check "Resource dosyalarinda sabit credential/secret olabilecek deger bulundu."
} else {
    Pass-Check "Resource dosyalarinda sabit credential/secret bulunmadi."
}

$permitAllFindings = Get-ChildItem -Path $javaRoot -Recurse -Filter *.java |
    Select-String -Pattern "permitAll\(" -CaseSensitive:$false

if ($permitAllFindings) {
    $permitAllFindings | ForEach-Object {
        Write-Host "$($_.Path):$($_.LineNumber) $($_.Line.Trim())" -ForegroundColor Cyan
    }
    Pass-Check "permitAll kullanimlari listelendi; sadece login/public endpointler olmali."
} else {
    Pass-Check "permitAll kullanimi bulunmadi."
}

if ($failed) {
    exit 1
}

Write-Host "Security check tamamlandi."
