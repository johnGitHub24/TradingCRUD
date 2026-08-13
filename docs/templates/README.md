# 可複用範本目錄

複製到其他 **Spring Boot + Vue + IntelliJ** 專案時使用。

| 檔案 | 說明 |
|------|------|
| [SpringBoot-Vue-IntelliJ-啟動設定範本.md](SpringBoot-Vue-IntelliJ-啟動設定範本.md) | **主文件**：變數表、程式片段、SOP、FAQ |
| [H2-Console-登入範本.md](H2-Console-登入範本.md) | **H2 Console 登入、90149 排錯、驗證 SQL** |
| [套用檢查表.md](套用檢查表.md) | 新專案逐項打勾 |
| [files/](files/) | 可直接複製的 `.template` 檔 |

### `files/` 檔案一覽

| 檔案 | 說明 |
|------|------|
| `StartupInfoLogger.java.template` | 啟動印 URL（讀 `startup.info.*`；片段見 EOS templates-index） |
| `TradingCrudApplication.xml.template` | Spring Boot Run Config |
| `Frontend__Vite_.xml.template` | Gradle 前端 |
| `Full_Stack.xml.template` | 複合 Run Config |
| `start.ps1.template` / `start-frontend.ps1.template` | 啟動腳本 |
| `application-h2.yml.template` | H2 datasource 設定 |
| `h2-console-驗證.sql.template` | H2 驗證 SQL |

## 快速套用

1. 開啟 `SpringBoot-Vue-IntelliJ-啟動設定範本.md`，填 §1 變數表
2. 從 `files/` 複製 template，全域替換 `{{...}}`
3. 依主文件 §3–§5 貼上程式片段
4. 用 `套用檢查表.md` 驗證

## 參考實作

本 repo（TradingCRUD）已完整套用，可直接對照：

- `src/main/java/com/trading/crud/config/StartupInfoLogger.java`
- `.idea/runConfigurations/`
- `build.gradle` → `startFrontend`
- `frontend/vite.config.js`、`stores/auth.js`、`router/index.js`、`api/client.js`
- `scripts/start.ps1`、`scripts/start-frontend.ps1`
