# ═══════════════════════════════════════════════════════════════════════════
# env.ps1 — 設定 JDK 21 開發環境變數
# ═══════════════════════════════════════════════════════════════════════════
#
# 【什麼】在本 PowerShell 工作階段設定 JAVA_HOME 與 PATH，讓系統找到 Java 21。
#
# 【為什麼需要】
#   TradingCRUD 後端是 Spring Boot，需要 JDK 21 才能執行 gradlew bootRun、編譯等。
#   若電腦裝了多個 Java 版本，不設定可能用到錯誤版本而編譯失敗。
#
# 【何時執行】
#   - 不要直接雙擊執行！
#   - 由其他腳本「點選載入」：開頭的 `. "$PSScriptRoot\env.ps1"`
#   - 或手動在專案根目錄執行：`. .\scripts\env.ps1`（注意最前面的點與空格）
#
# 【「點選載入」是什麼？】
#   PowerShell 中 `.` 表示「在目前工作階段執行腳本內容」，
#   這樣設定的 $env:JAVA_HOME 才會留在當前視窗，子程序也能繼承。
#   若用 `.\scripts\env.ps1`（沒有前面的點），變數可能不會保留。
#
# 【用法範例】
#   . .\scripts\env.ps1

# 預期 JDK 21 的安裝路徑（Windows 預設 Oracle/OpenJDK 安裝位置）
$jdkPath = "C:\Program Files\Java\jdk-21"

# Test-Path：檢查資料夾是否存在，避免設定到不存在的路徑
if (Test-Path $jdkPath) {
    # JAVA_HOME：Java 生態系標準變數，Gradle、Maven、IDE 都會讀取
    $env:JAVA_HOME = $jdkPath

    # 把 JDK 的 bin（java.exe、javac.exe）加到 PATH 最前面，優先使用此版本
    $env:PATH = "$jdkPath\bin;$env:PATH"

    # Write-Host：在終端機顯示訊息；-ForegroundColor 改變文字顏色（青色 = 資訊）
    Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Cyan
} else {
    # JDK 找不到時顯示警告，但不中斷腳本（讓呼叫者自行決定是否繼續）
    Write-Warning "JDK 21 not found at: $jdkPath"
}
