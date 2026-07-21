# ═══════════════════════════════════════════════════════════════════════════
# fix-intellij-registry.ps1 — 修復 IntelliJ IDEA JavaScript 外掛 Registry 錯誤
# ═══════════════════════════════════════════════════════════════════════════
#
# 【修復的錯誤】
#   MissingResourceException: Registry key node_modules.use.workspace.model is not defined
#
# 【原因】
#   IntelliJ「JavaScript and TypeScript」外掛版本與 IDE 核心不完全匹配時，
#   會嘗試讀取尚未定義的 Registry 鍵值而崩潰或無法索引 node_modules。
#
# 【本腳本做什麼】
#   在使用者設定目錄的 registry.xml 加入：
#   node_modules.use.workspace.model = false
#
# 【何時執行】
#   - 在 IntelliJ 開啟本專案時出現上述 Registry 錯誤
#   - 執行：.\scripts\fix-intellij-registry.ps1
#   - 執行後必須完全關閉並重新啟動 IntelliJ IDEA
#
# 【若仍失敗】
#   Settings → Plugins → 更新「JavaScript and TypeScript」外掛

# $env:APPDATA 通常是 C:\Users\<使用者>\AppData\Roaming
# JetBrains 各 IDE 的設定都存放在此下的 JetBrains 子資料夾
$jetbrainsDir = Join-Path $env:APPDATA "JetBrains"

if (-not (Test-Path $jetbrainsDir)) {
    Write-Error "JetBrains config not found: $jetbrainsDir"
    exit 1
}

# 找出所有 IntelliJIdea* 設定目錄（可能有多個版本並存）
# Sort-Object Name -Descending：版本號較新的目錄排前面
$ideaDirs = Get-ChildItem $jetbrainsDir -Directory -Filter "IntelliJIdea*" | Sort-Object Name -Descending

if ($ideaDirs.Count -eq 0) {
    Write-Error "No IntelliJIdea* config folder found under $jetbrainsDir"
    exit 1
}

# 要寫入 registry.xml 的鍵與值（關閉 workspace model 對 node_modules 的特殊處理）
$key = "node_modules.use.workspace.model"
$value = "false"
$fixed = 0

# 對每個找到的 IDEA 版本目錄嘗試修復（本機若只裝一個版本，通常只會處理一個）
foreach ($dir in $ideaDirs) {
    $optionsDir = Join-Path $dir.FullName "options"
    if (-not (Test-Path $optionsDir)) { continue }

    $registryFile = Join-Path $optionsDir "registry.xml"

    # registry.xml 內的 entry 行格式（注意反引號 ` 是 PowerShell 跳脫雙引號）
    $entry = "    <entry key=`"$key`" value=`"$value`" />"

    if (Test-Path $registryFile) {
        # 檔案已存在：讀取全文，檢查是否已有此 key
        $content = Get-Content $registryFile -Raw -Encoding UTF8
        if ($content -match [regex]::Escape($key)) {
            Write-Host "[skip] $($dir.Name) - key already present"
            continue
        }
        # 在 </component> 結束標籤前插入新 entry
        if ($content -match '</component>') {
            $content = $content -replace '</component>', "$entry`r`n  </component>"
            Set-Content -Path $registryFile -Value $content -Encoding UTF8
            Write-Host "[OK] Updated $($dir.Name)\options\registry.xml"
            $fixed++
        }
    } else {
        # 檔案不存在：建立最小可用的 registry.xml
        $xml = @"
<application>
  <component name="Registry">
$entry
  </component>
</application>
"@
        Set-Content -Path $registryFile -Value $xml -Encoding UTF8
        Write-Host "[OK] Created $($dir.Name)\options\registry.xml"
        $fixed++
    }
}

# 總結：有更新則提示重啟 IDE；完全沒更新則建議改更新外掛
if ($fixed -eq 0) {
    Write-Warning "No registry file updated. Try: Settings -> Plugins -> Update 'JavaScript and TypeScript'"
} else {
    Write-Host "`nDone. Please restart IntelliJ IDEA completely."
}
