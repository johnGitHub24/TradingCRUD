# ═══════════════════════════════════════════════════════════════════════════
# check.ps1 — 一鍵驗證（對齊 CI / Definition of Done）
# ═══════════════════════════════════════════════════════════════════════════
#
# 【什麼】執行 Gradle 的 `check` 任務：單元測試 + 整合測試 + 靜態檢查等。
#
# 【為什麼執行】
#   - 提交程式碼前確認沒有破壞既有功能
#   - 本機結果應與 CI 流水線一致（同樣跑 gradlew check）
#
# 【何時執行】
#   - 改完 Java 程式碼後
#   - 建立 Pull Request 之前
#   - 在專案根目錄執行：.\scripts\check.ps1
#
# 【成功標準】
#   - 終端機顯示 "[OK] check passed"
#   - 退出碼（exit code）為 0；非 0 表示有測試或檢查失敗
#
# 【PowerShell 初學提示】
#   $PSScriptRoot     — 目前腳本所在目錄（scripts/）
#   $LASTEXITCODE     — 上一個外部程式（如 gradlew）的退出碼
#   Set-Location       — 等同 cd，切換工作目錄

# 點選載入 env.ps1，確保本工作階段有正確的 JAVA_HOME
. "$PSScriptRoot\env.ps1"

# Split-Path -Parent：取得上一層目錄，從 scripts/ 回到專案根目錄
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "`n=== TradingCRUD: gradlew check ===" -ForegroundColor Yellow

# .\gradlew.bat check — Windows 下 Gradle Wrapper，不需全域安裝 Gradle
# "check" 是 Gradle 內建生命週期任務，會跑測試與相關驗證
.\gradlew.bat check

# 若 gradlew 失敗（退出碼非 0），把相同退出碼傳給呼叫者（CI 或 IDE 可偵測失敗）
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "`n[OK] check passed" -ForegroundColor Green
