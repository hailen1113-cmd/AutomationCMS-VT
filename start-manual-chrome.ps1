param(
    [int]$Port = 9222,
    [string]$ProfileDirectory = ".selenium/chrome-profile-manual-attach",
    [string]$Url = "https://erp-sandbox.vuatho.com/"
)

$ErrorActionPreference = "Stop"

$chromeCandidates = @(
    "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
    "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
    "$env:LOCALAPPDATA\Google\Chrome\Application\chrome.exe"
)
$chrome = $chromeCandidates |
    Where-Object { $_ -and (Test-Path -LiteralPath $_) } |
    Select-Object -First 1

if (-not $chrome) {
    throw "Khong tim thay Google Chrome."
}

$profile = [System.IO.Path]::GetFullPath(
    (Join-Path (Get-Location) $ProfileDirectory)
)
New-Item -ItemType Directory -Path $profile -Force | Out-Null

$debugEndpoint = "http://127.0.0.1:$Port/json/version"
try {
    Invoke-RestMethod -Uri $debugEndpoint -TimeoutSec 2 | Out-Null
    Write-Host "Chrome manual dang chay tai cong $Port."
    Write-Host "Hay dung cua so Chrome dang mo de dang nhap."
    exit 0
} catch {
    # Chưa có Chrome lắng nghe ở cổng này; tiếp tục khởi động.
}

$arguments = @(
    "--remote-debugging-port=$Port",
    "--user-data-dir=$profile",
    "--profile-directory=Default",
    "--disable-background-mode",
    "--new-window",
    $Url
)

Start-Process -FilePath $chrome -ArgumentList $arguments

Write-Host "Da mo Chrome thu cong: $Url"
Write-Host "Profile: $profile"
Write-Host "Remote debugging: 127.0.0.1:$Port"
Write-Host ""
Write-Host "1. Dang nhap Vercel/Google va vao Dashboard trong cua so nay."
Write-Host "2. KHONG dong Chrome."
Write-Host "3. Mo terminal khac va chay:"
Write-Host '   mvn test "-Dtest=LoginDashboardSourceAccessTest" "-Dchrome.debugger.address=127.0.0.1:9222" "-Dkeep.browser.open=true"'
