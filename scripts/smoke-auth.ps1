$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8080'
$pass = 0
$fail = 0

function Call($name, $expectedCode, $scriptblock) {
    try {
        $r = & $scriptblock
        $code = $r.code
    } catch {
        $body = $_.ErrorDetails.Message
        if (-not $body) { $body = $_.Exception.Message }
        $code = "EXCEPTION: $body"
    }
    if ("$code" -eq "$expectedCode") {
        $script:pass++
        Write-Host ("[PASS] {0,-38} code={1}" -f $name, $code) -ForegroundColor Green
    } else {
        $script:fail++
        Write-Host ("[FAIL] {0,-38} expect={1} got={2}" -f $name, $expectedCode, $code) -ForegroundColor Red
    }
    return $r
}

function Login($u, $p) {
    $b = @{ username = $u; password = $p } | ConvertTo-Json -Compress
    return Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post `
        -ContentType 'application/json; charset=utf-8' `
        -Body ([System.Text.Encoding]::UTF8.GetBytes($b))
}

Write-Host "`n===== 登录接口 =====" -ForegroundColor Cyan
$login = Call '1 登录成功 admin/123456' 0 { Login 'admin' '123456' }
Call '2 密码错误'            4001 { Login 'admin' 'wrong123' }
Call '3 用户不存在'          4001 { Login 'nobody_xyz' '123456' }
Call '4 用户名空白(@Valid)'  4000 { Login '' '123456' }

$token = $login.data.token
Write-Host "  token 前 40 位: $($token.Substring(0,40))..."
Write-Host "  userId=$($login.data.userId) nickname=$($login.data.nickname) role=$($login.data.role)"

Write-Host "`n===== /api/auth/me 鉴权链 =====" -ForegroundColor Cyan
Call '5 不带 token'          401 { Invoke-RestMethod -Uri "$base/api/auth/me" -Method Get }
Call '6 带正确 token'          0 { Invoke-RestMethod -Uri "$base/api/auth/me" -Method Get -Headers @{ Authorization = "Bearer $token" } }
Call '7 带假 token'          401 { Invoke-RestMethod -Uri "$base/api/auth/me" -Method Get -Headers @{ Authorization = 'Bearer abc.def.ghi' } }
Call '8 Header 缺 Bearer 前缀' 401 { Invoke-RestMethod -Uri "$base/api/auth/me" -Method Get -Headers @{ Authorization = $token } }

Write-Host "`n===== 放行接口 & 异常兜底 =====" -ForegroundColor Cyan
Call '9 /api/ping 免登录'      0 { Invoke-RestMethod -Uri "$base/api/ping" -Method Get }
Call '10 不存在的接口(带token)' 404 { Invoke-RestMethod -Uri "$base/api/tickets" -Method Get -Headers @{ Authorization = "Bearer $token" } }
Call '11 方法不支持(POST /me)'  405 { Invoke-RestMethod -Uri "$base/api/auth/me" -Method Post -Headers @{ Authorization = "Bearer $token" } }
Call '12 坏 JSON'             400 {
    Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post `
        -ContentType 'application/json; charset=utf-8' `
        -Body ([System.Text.Encoding]::UTF8.GetBytes('{"username":admin}'))
}

Write-Host "`n===== 结果: PASS=$pass  FAIL=$fail =====" -ForegroundColor $(if ($fail -eq 0) { 'Green' } else { 'Yellow' })
