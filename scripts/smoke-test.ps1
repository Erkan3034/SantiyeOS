param(
    [string] $BaseUrl = $env:SANTIYEOS_BASE_URL,
    [string] $Email = $env:SANTIYEOS_EMAIL,
    [string] $Password = $env:SANTIYEOS_PASSWORD,
    [int] $FirmaId = $(if ($env:SANTIYEOS_FIRMA_ID) { [int] $env:SANTIYEOS_FIRMA_ID } else { 1 })
)

if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
    $BaseUrl = "http://localhost:8081"
}

if ([string]::IsNullOrWhiteSpace($Email) -or [string]::IsNullOrWhiteSpace($Password)) {
    throw "SANTIYEOS_EMAIL ve SANTIYEOS_PASSWORD env degerlerini giriniz."
}

$ErrorActionPreference = "Stop"

function Invoke-SantiyeRequest {
    param(
        [string] $Name,
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null
    )

    $parameters = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
    }

    if ($null -ne $Body) {
        $parameters["ContentType"] = "application/json"
        $parameters["Body"] = ($Body | ConvertTo-Json -Depth 8)
    }

    Invoke-RestMethod @parameters | Out-Null
    Write-Host "[OK] $Name"
}

$loginBody = @{
    email = $Email
    sifre = $Password
}

$loginResponse = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/auth/login" `
    -ContentType "application/json" `
    -Body ($loginBody | ConvertTo-Json)

$headers = @{
    Authorization = "Bearer $($loginResponse.accessToken)"
    "X-Firma-Id" = "$FirmaId"
}

Write-Host "[OK] Login"

Invoke-SantiyeRequest -Name "Auth/me" -Method Get -Uri "$BaseUrl/api/auth/me" -Headers $headers
Invoke-SantiyeRequest -Name "Abonelik planlari" -Method Get -Uri "$BaseUrl/api/abonelik-planlari?aktif=true" -Headers $headers
Invoke-SantiyeRequest -Name "Projeler" -Method Get -Uri "$BaseUrl/api/projeler?limit=5&offset=0" -Headers $headers
Invoke-SantiyeRequest -Name "Taseronlar" -Method Get -Uri "$BaseUrl/api/taseronlar?limit=5&offset=0" -Headers $headers
Invoke-SantiyeRequest -Name "Is emirleri" -Method Get -Uri "$BaseUrl/api/is-emirleri?limit=5&offset=0" -Headers $headers
Invoke-SantiyeRequest -Name "Hakedisler" -Method Get -Uri "$BaseUrl/api/hakedisler?limit=5&offset=0" -Headers $headers
Invoke-SantiyeRequest -Name "Odemeler" -Method Get -Uri "$BaseUrl/api/odemeler?limit=5&offset=0" -Headers $headers

Write-Host "Smoke test tamamlandi."
