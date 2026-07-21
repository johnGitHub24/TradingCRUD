# ═══════════════════════════════════════════════════════════════════════════
# export-openapi.ps1 — 從執行中的後端匯出 OpenAPI JSON 規格檔
# ═══════════════════════════════════════════════════════════════════════════
#
# 【什麼】向 Spring Boot 的 /v3/api-docs 下載 OpenAPI 描述，存成 docs/openapi.json。
#
# 【為什麼需要】
#   - 版本控管 API 契約（與前端、文件、契約測試對齊）
#   - 離線查看 API 結構，不必開 Swagger UI
#   - CI 或程式碼產生器可讀取此 JSON
#
# 【何時執行】
#   - 後端已啟動於 :8083（.\gradlew.bat bootRun 或 scripts/start.ps1）
#   - 新增或修改 Controller API 後，想更新文件快照時
#   - 執行：.\scripts\export-openapi.ps1
#
# 【輸出位置】
#   docs/openapi.json（相對專案根目錄）

# 載入 JDK 環境（與其他腳本慣例一致）
. "$PSScriptRoot\env.ps1"

$root = Split-Path $PSScriptRoot -Parent
$outFile = Join-Path $root "docs\openapi.json"

Write-Host "從 http://localhost:8083/v3/api-docs 匯出..." -ForegroundColor Cyan

try {
    # Invoke-RestMethod -OutFile：下載 JSON 並直接寫入檔案（不經過記憶體物件轉換）
    Invoke-RestMethod -Uri "http://localhost:8083/v3/api-docs" -OutFile $outFile
    Write-Host "✅ 已寫入 $outFile" -ForegroundColor Green
} catch {
    # catch：後端未啟動、網路錯誤、404 等會進入此區塊
    Write-Error "匯出失敗，請確認後端已啟動：.\gradlew.bat bootRun"
    exit 1
}
