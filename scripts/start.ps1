# ═══════════════════════════════════════════════════════════════════════════
# start.ps1 — 啟動後端 + 前端開發環境（一鍵開兩個新視窗）
# ═══════════════════════════════════════════════════════════════════════════
#
# 【什麼】同時啟動 Spring Boot（:8083）與 Vue Vite 開發伺服器（:5173）。
#
# 【為什麼用這個腳本】
#   開發時前後端要一起跑；手動開兩個終端機很麻煩，此腳本自動開新視窗分別執行。
#
# 【何時執行】
#   - 每天開始開發 TradingCRUD 時
#   - 在專案根目錄執行：.\scripts\start.ps1
#
# 【停止方式】
#   - 關閉彈出的兩個 PowerShell 視窗，或在各視窗按 Ctrl+C
#   - 本腳本執行完會結束，不會佔用目前這個終端機
#
# 【與 start-frontend.ps1 的差異】
#   - start.ps1：後端 + 前端都啟動（完整開發環境）
#   - start-frontend.ps1：只啟動前端（後端需已手動運行）

# 載入 JDK 21 環境（Spring Boot / Gradle 需要）
. "$PSScriptRoot\env.ps1"

# $root = 專案根目錄（TradingCRUD/）
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

Write-Host "啟動 Spring Boot（:8083）..." -ForegroundColor Cyan

# Start-Process：開啟新的 PowerShell 視窗執行命令，不阻塞目前腳本
# -NoExit：命令跑完後視窗不關閉（方便看 log、Ctrl+C 停止）
# -Command：要在新視窗執行的 PowerShell 指令字串
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root'; .\gradlew.bat bootRun"

# Start-Sleep：等待 8 秒，讓 Spring Boot 有時間啟動，再開前端（減少前端先連後端失敗）
Start-Sleep -Seconds 8

# Join-Path：安全拼接路徑（自動處理反斜線）
$frontendDir = Join-Path $root "frontend"

# 只有 frontend 資料夾存在時才啟動 Vite（避免目錄結構異常時報錯）
if (Test-Path $frontendDir) {
    Write-Host "啟動 Vue 前端（:5173）..." -ForegroundColor Cyan

    # 新視窗：進入 frontend、若無 node_modules 則 npm install、再 npm run dev
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; if (-not (Test-Path node_modules)) { npm install }; npm run dev"
}

# 以下僅顯示常用網址與預設帳號，方便複製到瀏覽器
Write-Host "`n========== TradingCRUD 使用連結 ==========" -ForegroundColor Green
Write-Host "【後端 :8083】"
Write-Host "  健康檢查     http://localhost:8083/actuator/health"
Write-Host "  應用資訊     http://localhost:8083/actuator/info"
Write-Host "  Swagger UI   http://localhost:8083/swagger-ui.html"
Write-Host "  OpenAPI JSON http://localhost:8083/v3/api-docs"
Write-Host "  H2 Console   http://localhost:8083/h2-console"
Write-Host "  H2 JDBC URL  jdbc:h2:mem:tradingcrud  (sa / 空白密碼)"
Write-Host "【前台 :5173】"
Write-Host "  登入頁       http://localhost:5173/login"
Write-Host "  訂單頁       http://localhost:5173/orders"
Write-Host "  預設帳號     admin / admin123"
Write-Host "=========================================="
