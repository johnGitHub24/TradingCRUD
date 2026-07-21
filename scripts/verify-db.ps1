# ═══════════════════════════════════════════════════════════════════════════
# verify-db.ps1 — 資料庫讀寫驗證（API Smoke Test）
# ═══════════════════════════════════════════════════════════════════════════
#
# 【什麼】透過 REST API 做一輪「登入 → 新增訂單 → 查詢 → 列表 → 刪除」，
#         間接驗證 H2 記憶體資料庫與 JPA 層是否正常。
#
# 【為什麼不用 H2 Console 腳本化】
#   H2 Console 是網頁介面，不適合自動化；用 API 測試更接近真實使用情境。
#
# 【何時執行】
#   - Spring Boot 已啟動於 :8083 之後
#   - 改過 Entity、Repository、Order API 後想快速確認 DB 讀寫
#   - 執行：.\scripts\verify-db.ps1
#
# 【失敗時】
#   - 腳本會拋出錯誤或 Invoke-RestMethod 顯示 HTTP 錯誤
#   - 先確認後端健康：http://localhost:8083/actuator/health

# 載入 JDK 環境（與其他腳本一致；本腳本主要呼叫 HTTP，但保持慣例）
. "$PSScriptRoot\env.ps1"

# API 基底路徑（Spring Boot 的 REST 前綴）
$base = "http://localhost:8083/api/v1"

Write-Host "`n=== DB Smoke Test（API 層驗證 DB 讀寫）===" -ForegroundColor Yellow

# ── 步驟 1：登入取得 JWT ─────────────────────────────────────────────────
# Invoke-RestMethod：PowerShell 內建 HTTP 客戶端，會自動把 JSON 回應轉成物件
$loginBody = '{"username":"admin","password":"admin123"}'
$login = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
$token = $login.accessToken

# 後續請求需在 Header 帶 Bearer token（Spring Security 驗證用）
$headers = @{ Authorization = "Bearer $token" }
Write-Host "[OK] AUTH-001 login success role=$($login.role)"

# ── 步驟 2：新增訂單（寫入 DB）────────────────────────────────────────────
# 用時間戳產生唯一 clientOrderId，避免與舊資料衝突
$clientId = "smoke-$(Get-Date -Format 'yyyyMMddHHmmss')"

# PowerShell hashtable @{ } 轉 JSON 字串，作為 POST body
$orderBody = @{
    clientOrderId = $clientId
    symbol = "BTCUSDT"
    side = "BUY"
    quantity = 0.1
    price = 65000
} | ConvertTo-Json

$created = Invoke-RestMethod -Uri "$base/orders" -Method POST -Headers $headers -ContentType "application/json" -Body $orderBody
Write-Host "[OK] ORDER-001 create success id=$($created.id)"

# ── 步驟 3：依 ID 查詢單筆（讀取 DB）──────────────────────────────────────
$fetched = Invoke-RestMethod -Uri "$base/orders/$($created.id)" -Headers $headers

# 比對 clientOrderId 是否與建立時一致，不一致代表資料錯亂
if ($fetched.clientOrderId -ne $clientId) { throw "查詢結果不符" }
Write-Host "[OK] ORDER-002 query success"

# ── 步驟 4：分頁列表（讀取多筆）────────────────────────────────────────────
$list = Invoke-RestMethod -Uri "$base/orders?page=0&size=5" -Headers $headers
Write-Host "[OK] ORDER-008 list success total=$($list.meta.total)"

# ── 步驟 5：刪除測試資料（清理 DB，避免 smoke 訂單累積）────────────────────
Invoke-RestMethod -Uri "$base/orders/$($created.id)" -Method DELETE -Headers $headers
Write-Host "[OK] ORDER-007 delete success"

Write-Host "`n[OK] DB smoke test passed" -ForegroundColor Green
