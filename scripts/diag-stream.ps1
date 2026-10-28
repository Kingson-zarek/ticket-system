# Stream (SSE) diagnostic for one provider. ASCII only - see diag-providers.ps1 header.
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts\diag-stream.ps1
#   powershell -ExecutionPolicy Bypass -File scripts\diag-stream.ps1 -Model glm-5.2

param(
    [string]$Base  = 'https://api.697351.xyz/v1',
    [string]$KeyVar = 'CAROLINE_API_KEY',
    [string]$Model = 'deepseek-v4.1-flash'
)

$credFile = Join-Path $env:USERPROFILE '.dsh\.credentials.yaml'
$creds = @{}
Get-Content $credFile -Encoding UTF8 | ForEach-Object {
    if ($_ -match '^([A-Z_]+):\s*(.+)$') { $creds[$Matches[1]] = $Matches[2].Trim() }
}
$key = $creds[$KeyVar]
if (-not $key) { Write-Host "missing key $KeyVar"; exit 1 }

Write-Host ('provider : ' + $Base)
Write-Host ('model    : ' + $Model)
Write-Host ''

# ---------- 1. full model list ----------
Write-Host '---- [1] full model list ----' -ForegroundColor Cyan
try {
    $m = Invoke-RestMethod -Uri ($Base + '/models') -Headers @{ Authorization = 'Bearer ' + $key } -TimeoutSec 20
    $m.data | ForEach-Object { Write-Host ('  ' + $_.id) }
} catch {
    Write-Host ('  FAILED: ' + $_.Exception.Message) -ForegroundColor Red
}

# ---------- 2. non-stream, generous max_tokens ----------
Write-Host ''
Write-Host '---- [2] non-stream, max_tokens=200 ----' -ForegroundColor Cyan
$payload = @{
    model      = $Model
    messages   = @(@{ role = 'user'; content = 'Reply with exactly: OK' })
    max_tokens = 200
} | ConvertTo-Json -Depth 5 -Compress

$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
    $r = Invoke-RestMethod -Uri ($Base + '/chat/completions') -Method Post `
        -Headers @{ Authorization = 'Bearer ' + $key } `
        -ContentType 'application/json; charset=utf-8' `
        -Body ([System.Text.Encoding]::UTF8.GetBytes($payload)) -TimeoutSec 90
    $sw.Stop()
    Write-Host ('  elapsed  : ' + $sw.ElapsedMilliseconds + ' ms')
    Write-Host ('  content  : [' + $r.choices[0].message.content + ']')
    Write-Host ('  reasoning: [' + $r.choices[0].message.reasoning_content + ']')
    Write-Host ('  finish   : ' + $r.choices[0].finish_reason)
    Write-Host ('  usage    : ' + ($r.usage | ConvertTo-Json -Compress))
} catch {
    $sw.Stop()
    Write-Host ('  FAILED after ' + $sw.ElapsedMilliseconds + ' ms: ' + $_.Exception.Message) -ForegroundColor Red
    if ($_.ErrorDetails.Message) { Write-Host ('  body: ' + $_.ErrorDetails.Message) }
}

# ---------- 3. stream SSE, watch for stalls ----------
Write-Host ''
Write-Host '---- [3] stream=true (SSE), 60s cap ----' -ForegroundColor Cyan
$payload2 = @{
    model      = $Model
    messages   = @(@{ role = 'user'; content = 'Reply with exactly: OK' })
    max_tokens = 200
    stream     = $true
} | ConvertTo-Json -Depth 5 -Compress
$tmp = Join-Path $env:TEMP 'sse-body.json'
[System.IO.File]::WriteAllText($tmp, $payload2, (New-Object System.Text.UTF8Encoding($false)))

$out = Join-Path $env:TEMP 'sse-out.txt'
$sw2 = [System.Diagnostics.Stopwatch]::StartNew()
& curl.exe -sS -N -m 60 --no-buffer -X POST ($Base + '/chat/completions') `
    -H ('Authorization: Bearer ' + $key) `
    -H 'Content-Type: application/json' `
    --data-binary ('@' + $tmp) 2>&1 | Tee-Object -FilePath $out | Out-Null
$sw2.Stop()

$lines = @()
if (Test-Path $out) { $lines = Get-Content $out -Encoding UTF8 }
Write-Host ('  elapsed  : ' + $sw2.ElapsedMilliseconds + ' ms')
Write-Host ('  lines    : ' + $lines.Count)

$dataLines = $lines | Where-Object { $_ -match '^\s*data:\s*\S' }
Write-Host ('  data evt : ' + $dataLines.Count)

if ($dataLines.Count -gt 0) {
    Write-Host '  first 3 events:'
    $dataLines | Select-Object -First 3 | ForEach-Object { Write-Host ('    ' + $_.Substring(0, [Math]::Min(220, $_.Length))) }
    Write-Host '  last 2 events:'
    $dataLines | Select-Object -Last 2 | ForEach-Object { Write-Host ('    ' + $_.Substring(0, [Math]::Min(220, $_.Length))) }
} else {
    Write-Host '  NO data: lines received - raw output follows:' -ForegroundColor Red
    $lines | Select-Object -First 20 | ForEach-Object { Write-Host ('    ' + $_) }
}

Write-Host ''
Write-Host '===== done =====' -ForegroundColor Cyan
