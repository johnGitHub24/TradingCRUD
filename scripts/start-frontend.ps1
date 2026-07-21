# ═══════════════════════════════════════════════════════════════════════════
# start-frontend.ps1 — 僅啟動 Vue Vite 開發伺服器（:5173）
# ═══════════════════════════════════════════════════════════════════════════
#
# 【什麼】在目前這個終端機執行 `npm run dev`，啟動前端熱更新開發伺服器。
#
# 【為什麼單獨存在】
#   有時後端已在 IDE 或另一個視窗跑著，只需重啟或單獨開前端即可。
#
# 【何時執行】
#   - Spring Boot 已在 :8083 運行時
#   - 在專案根目錄執行：.\scripts\start-frontend.ps1
#
# 【與 start.ps1 的差異】
#   - 本腳本：只開前端，會佔用「目前」終端機直到 Ctrl+C
#   - start.ps1：後端+前端，各開新視窗，本終端機可繼續做別的事
#
# 【前提】
#   後端 API 必須在 http://localhost:8083（Vite 會透過 proxy 轉發 /api）

# 計算專案根目錄與 frontend 子目錄
$root = Split-Path $PSScriptRoot -Parent
$frontendDir = Join-Path $root "frontend"

# 切換到 frontend，後續 npm 指令都在此目錄執行
Set-Location $frontendDir

# 首次 clone 專案時通常沒有 node_modules，需先安裝依賴
if (-not (Test-Path "node_modules")) {
    Write-Host "首次執行，安裝 npm 依賴..." -ForegroundColor Yellow
    npm install
}

# 顯示開發用 URL 與預設登入帳號
Write-Host ""
Write-Host "========== TradingCRUD 前端 ==========" -ForegroundColor Cyan
Write-Host "登入頁    : http://localhost:5173/login"
Write-Host "預設帳號  : admin / admin123"
Write-Host "（後端需同時運行於 :8083）"
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 啟動 Vite；此命令會持續執行，修改 .vue/.ts 檔會自動熱更新
npm run dev
