# build-learning-html.ps1 — 產生 docs/TradingCRUD-完整學習手冊.html
# 用法：.\scripts\build-learning-html.ps1

$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
node scripts/build-learning-html.mjs

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n完成！請用瀏覽器開啟：" -ForegroundColor Green
    Write-Host "  docs\TradingCRUD-完整學習手冊.html"
}
