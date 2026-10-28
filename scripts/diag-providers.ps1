# Provider diagnostic: test each provider's /models and /chat/completions
# Usage: powershell -ExecutionPolicy Bypass -File scripts\diag-providers.ps1
# Requires Windows PowerShell 5.1+ / PowerShell 7+. ASCII only on purpose -
# PS 5.1 decodes .ps1 as GBK on a Chinese Windows, so non-ASCII breaks parsing.

$ErrorActionPreference = 'Continue'

$providers = @(
    @{ name = 'caroline'; base = 'https://api.697351.xyz/v1';   keyVar = 'CAROLINE_API_KEY'; model = 'deepseek-v4.1-flash' }
    @{ name = 'hyx';      base = 'https://hyxware.com/v1';      keyVar = 'HYX_API_KEY';      model = 'deepseek-v4.1-flash' }
    @{ name = 'hkai';     base = 'https://share.hkai25.top/v1'; keyVar = 'HKAI_API_KEY';     model = 'deepseek-v4.1-flash' }
    @{ name = 'pangmao';  base = 'https://wzw.pp.ua/v1';        keyVar = 'PANGMAO_API_KEY';  model = 'deepseek-v4-flash' }
    @{ name = 'official'; base = 'https://api.deepseek.com/v1'; keyVar = 'DEEPSEEK_API_KEY'; model = 'deepseek-v4-flash' }
)

$credFile = Join-Path $env:USERPROFILE '.dsh\.credentials.yaml'
$creds = @{}
if (Test-Path $credFile) {
    Get-Content $credFile -Encoding UTF8 | ForEach-Object {
        if ($_ -match '^([A-Z_]+):\s*(.+)$') { $creds[$Matches[1]] = $Matches[2].Trim() }
    }
}

foreach ($p in $providers) {
    $key = $creds[$p.keyVar]
    Write-Host ''
    Write-Host ('========== ' + $p.name + ' ==========') -ForegroundColor Cyan
    Write-Host ('baseURL : ' + $p.base)

    if (-not $key) {
        Write-Host ('key     : MISSING (' + $p.keyVar + ' not configured)') -ForegroundColor Yellow
        continue
    }
    $masked = $key.Substring(0, 6) + '...' + $key.Substring($key.Length - 4)
    Write-Host ('key     : ' + $masked + ' (len=' + $key.Length + ')')

    # ---- 1. GET /models ----
    $models = $null
    try {
        $r = Invoke-RestMethod -Uri ($p.base + '/models') -Headers @{ Authorization = 'Bearer ' + $key } -TimeoutSec 20 -ErrorAction Stop
        $models = @($r.data | ForEach-Object { $_.id })
        Write-Host ('[models]  HTTP 200, ' + $models.Count + ' models') -ForegroundColor Green

        if ($models -contains $p.model) {
            Write-Host ('[models]  target model "' + $p.model + '" FOUND') -ForegroundColor Green
        } else {
            Write-Host ('[models]  target model "' + $p.model + '" NOT FOUND') -ForegroundColor Red
            $sample = $models | Select-Object -First 40
            Write-Host ('          available: ' + ($sample -join ', '))
        }
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        $body = $_.ErrorDetails.Message
        Write-Host ('[models]  FAILED HTTP=' + $code + '  ' + $_.Exception.Message) -ForegroundColor Red
        if ($body) {
            Write-Host ('          body: ' + $body.Substring(0, [Math]::Min(400, $body.Length)))
        }
    }

    # ---- 2. POST /chat/completions ----
    if ($models -and -not ($models -contains $p.model)) {
        Write-Host '[chat]    SKIPPED (model id not in list)' -ForegroundColor Yellow
        continue
    }

    $payload = @{
        model      = $p.model
        messages   = @(@{ role = 'user'; content = 'hi' })
        max_tokens = 8
    } | ConvertTo-Json -Depth 5 -Compress

    try {
        $r2 = Invoke-RestMethod -Uri ($p.base + '/chat/completions') -Method Post `
            -Headers @{ Authorization = 'Bearer ' + $key } `
            -ContentType 'application/json; charset=utf-8' `
            -Body ([System.Text.Encoding]::UTF8.GetBytes($payload)) `
            -TimeoutSec 40 -ErrorAction Stop
        $txt = $r2.choices[0].message.content
        Write-Host ('[chat]    OK  reply: ' + $txt) -ForegroundColor Green
        Write-Host ('          usage: ' + ($r2.usage | ConvertTo-Json -Compress))
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        $body = $_.ErrorDetails.Message
        Write-Host ('[chat]    FAILED HTTP=' + $code + '  ' + $_.Exception.Message) -ForegroundColor Red
        if ($body) {
            Write-Host ('          body: ' + $body.Substring(0, [Math]::Min(500, $body.Length)))
        }
    }
}

Write-Host ''
Write-Host '===== done =====' -ForegroundColor Cyan
