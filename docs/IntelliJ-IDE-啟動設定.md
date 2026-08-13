# IntelliJ IDEA 啟動設定指南（Spring Boot + 前端）

> **適用專案：** TradingCRUD 及所有 Gradle Spring Boot 單模組／多模組專案。  
> **問題類型：** IDE 只能跑錯誤的 Application、無法從 Console 啟動到 Web、Run 選單沒有正確主類。

---

## 1. 症狀對照

| 症狀 | 可能原因 |
|------|----------|
| Run 選單出現**別的專案**的 `XxxApplication`（如 TradingCardApplication） | 開錯資料夾、多專案工作區混淆、舊 Run Config 殘留 |
| 找不到 `TradingCrudApplication` | Gradle 未 Sync、JDK 不對、Spring Boot 外掛未載入 |
| `bootRun` 在 Terminal 可跑，IDE 不行 | Gradle JVM 與 Project SDK 不一致 |
| 後端能跑，前端 `:5173` 開不起來 | 未建立 npm Run Configuration |
| 綠色三角形灰色／無法點 | Module 未正確匯入（`TradingCRUD.main` 不存在） |

---

## 2. Root Cause 分析（TradingCRUD 實例）

### RC-1：開啟路徑錯誤（最常見）

```text
❌ 開啟 D:\ClaudeCode\                    ← 父資料夾，多專案混在一起
❌ 開啟 Cursor Multi-root Workspace      ← IDE 可能抓到其他專案主類
✅ 開啟 D:\ClaudeCode\TradingCRUD\        ← 專案根目錄（含 build.gradle）
```

IntelliJ 的 Run Configuration 是**依目前開啟的專案模組**產生。開父資料夾時，可能顯示其他 repo 的 `*Application`。

### RC-2：`build.gradle` 未明確指定 mainClass

Spring Boot Gradle Plugin 雖會自動偵測，但 IDE 在 Gradle Sync 失敗或快取過期時會抓不到。

**修正（已套用於本專案）：**

```gradle
springBoot {
    mainClass = 'com.trading.crud.TradingCrudApplication'
}
```

### RC-3：JDK 名稱不一致

| 設定位置 | 錯誤範例 | 正確 |
|----------|----------|------|
| Project Structure → SDK | 未設定 / JDK 8 | **JDK 21** |
| Gradle JVM（Settings → Build Tools → Gradle） | `openjdk-21`（機器上不存在此名稱） | 與 SDK 同名，如 `21` |
| `misc.xml` languageLevel | `JDK_19` | `JDK_21` |

Gradle 在 Terminal 用 `JAVA_HOME` 能跑，但 IDE 用**另一套 JDK 名稱**會 Sync 失敗。

### RC-4：Run Configuration 未納入版控

`.idea/workspace.xml` 裡的 Run Config **不應依賴**（已在 `.gitignore` 排除）。

**修正：** 使用 `.idea/runConfigurations/*.xml`（已納入版控）。

### RC-5：前端是獨立程序

Spring Boot 只啟動後端 `:8083`。Vue Vite 需**另一個** npm Run Configuration（`:5173`）。

---

## 3. 正確開啟專案（第一次）

### Step 1：只開專案根目錄

```
File → Open → 選擇 D:\ClaudeCode\TradingCRUD
```

確認 Project 視窗根節點名稱是 **TradingCRUD**，不是 ClaudeCode。

### Step 2：設定 JDK 21

```
File → Project Structure → Project
  SDK: 21 (或 jdk-21，依你本機安裝名稱)
  Language level: 21
```

```
File → Settings → Build, Execution, Deployment → Build Tools → Gradle
  Gradle JVM: 與上面相同（建議選 Project SDK）
```

### Step 3：Gradle Sync

```
右側 Gradle 面板 → 點擊 Reload（重新整理）
或：右鍵 build.gradle → Link Gradle Project
```

成功標誌：Gradle 樹狀出現 `TradingCRUD → Tasks → application → bootRun`。

### Step 4：選擇 Run Configuration

右上角下拉應出現（已預設在 repo 內）：

| 名稱 | 用途 | 網址 |
|------|------|------|
| **TradingCrudApplication** | Spring Boot 直接啟動（推薦） | http://localhost:8083 |
| **bootRun (TradingCRUD)** | 透過 Gradle bootRun | http://localhost:8083 |
| **Frontend (Vite)** | Vue 前端（Gradle `startFrontend`） | http://localhost:5173 |
| **Full Stack** | 同時啟動後端 + 前端 | 兩個 Run 分頁 |

> **Full Stack 注意：** 會開啟 **兩個** Run 視窗分頁——
> - `TradingCrudApplication`（後端 log，含網址提示）
> - `Frontend (Vite)`（應看到 `Local: http://localhost:5173/`）
>
> 若只有後端 log、`:5173` 連不上，表示前端未啟動。請單獨 Run → **Frontend (Vite)**，
> 或 Terminal 執行 `.\scripts\start-frontend.ps1`。

點綠色 ▶ 執行。

---

## 4. 從 Console 到 Web 完整流程

```text
┌─────────────────────────────────────────────────────────────┐
│ 方式 A：IntelliJ Full Stack（一鍵）                          │
│   Run → Full Stack → 後端 :8083 + 前端 :5173                 │
├─────────────────────────────────────────────────────────────┤
│ 方式 B：IntelliJ 分開啟動                                    │
│   1. Run → TradingCrudApplication                            │
│   2. Run → Frontend (Vite)                                   │
├─────────────────────────────────────────────────────────────┤
│ 方式 C：Terminal（不依賴 IDE）                               │
│   終端 1: .\gradlew.bat bootRun                              │
│   終端 2: cd frontend && npm install && npm run dev          │
├─────────────────────────────────────────────────────────────┤
│ 方式 D：PowerShell 腳本                                      │
│   .\scripts\start.ps1                                        │
└─────────────────────────────────────────────────────────────┘
```

**驗證：**

| 檢查 | URL |
|------|-----|
| 後端健康 | http://localhost:8083/actuator/health |
| Swagger | http://localhost:8083/swagger-ui.html |
| 前端 UI | http://localhost:5173 |
| 登入 | admin / admin123 |

---

## 5. 本專案已套用的檔案變更

| 檔案 | 變更 |
|------|------|
| `build.gradle` | `springBoot { mainClass = '...' }` |
| `gradle.properties` | JVM 參數、編碼 |
| `.gitignore` | 保留 `!.idea/runConfigurations/` |
| `.idea/runConfigurations/*.xml` | 4 組共用 Run Config |
| `.idea/misc.xml` | `languageLevel=JDK_21` |
| `.idea/gradle.xml` | `gradleJvm=21` |

---

## 6. 手動建立 Run Configuration（其他專案套用）

### 6.1 Spring Boot Application

```
Run → Edit Configurations → + → Spring Boot
  Name: XxxApplication
  Main class: com.example.XxxApplication   ← 從原始碼確認，勿猜
  Module: 專案名.main                       ← 如 TradingCRUD.main
  JRE: Project SDK (21)
```

### 6.2 Gradle bootRun（替代方案）

```
Run → Edit Configurations → + → Gradle
  Name: bootRun (專案名)
  Tasks: bootRun
  Gradle project: 專案根目錄
```

### 6.3 npm 前端（有 frontend/ 時）

```
Run → Edit Configurations → + → npm
  Name: Frontend (Vite)
  package.json: $PROJECT_DIR$/frontend/package.json
  Command: run
  Scripts: dev
```

### 6.4 Compound 全棧

```
Run → Edit Configurations → + → Compound
  Name: Full Stack
  勾選：後端 Config + 前端 Config
```

**匯出到版控：** 上述設定會存成 `.idea/runConfigurations/檔名.xml`，可 commit 給團隊共用。

---

## 7. 多模組專案（如 APIGatewayMQ）

`settings.gradle` 含多子專案時：

```gradle
rootProject.name = 'APIGatewayMQ'
include 'common', 'gateway', 'engine'
```

| 模組 | 主類別 | Gradle 任務 | 埠 |
|------|--------|-------------|-----|
| gateway | `com.trading.gateway.GatewayApplication` | `:gateway:bootRun` | 8080 |
| engine | `com.trading.engine.TradingEngineApplication` | `:engine:bootRun` | 8081 |

**每個可執行模組各建一組 Run Configuration**，Main class 與 Module 必須對應：

```text
gateway  → Module: APIGatewayMQ.gateway.main
engine   → Module: APIGatewayMQ.engine.main
```

`common` 模組**沒有** Application，不可選為啟動類。

---

## 8. 疑難排解 Checklist

```
[ ] 確認開啟的是專案根目錄（含 build.gradle / settings.gradle）
[ ] Project SDK = JDK 21
[ ] Gradle JVM = 與 Project SDK 相同
[ ] Gradle Sync 無紅字錯誤
[ ] build.gradle 有 springBoot { mainClass = '...' }
[ ] Run Configuration 的 Module 為 *.main（不是 *.test）
[ ] 主類別全名正確（區分大小寫：TradingCrudApplication）
[ ] 埠 8083 未被佔用（netstat -ano | findstr 8083）
[ ] 前端已 npm install（frontend/node_modules 存在）
[ ] File → Invalidate Caches → Restart（最後手段）
```

### 刪除錯誤的 Run Configuration

```
Run → Edit Configurations → 選到錯誤的（如 TradingCardApplication）→ - 刪除
```

若刪除後又出現，代表**開啟的專案範圍仍不對**，回到 Step 1 重新 Open。

---

## 9. settings.gradle 要不要改？

| 專案類型 | settings.gradle | 說明 |
|----------|-----------------|------|
| 單模組（TradingCRUD） | `rootProject.name = 'TradingCRUD'` | **不用加 include**，已足夠 |
| 多模組（APIGatewayMQ） | `include 'gateway', 'engine'` | 需為每模組建 Run Config |

**IDE 啟動問題 99% 不是 settings.gradle 寫錯**，而是 **JDK / 開啟路徑 / mainClass / Gradle Sync**。

---

## 10. 快速參考指令

```powershell
# 確認 JDK
. .\scripts\env.ps1
java -version

# 確認 Gradle 可啟動（與 IDE 無關的基線測試）
.\gradlew.bat bootRun

# 確認主類別
type build\resolvedMainClassName
# 應輸出：com.trading.crud.TradingCrudApplication
```

---

## 11. MissingResourceException：`node_modules.use.workspace.model`

### 症狀

關閉專案、Invalidate Caches、或開啟含 `frontend/` 的專案時，IDE 日誌出現：

```text
java.util.MissingResourceException: Registry key node_modules.use.workspace.model is not defined
  at com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager.shouldUseWorkspaceModel(...)
```

### Root Cause

| 項目 | 說明 |
|------|------|
| **本質** | IntelliJ **核心**與 **JavaScript and TypeScript 外掛**版本不一致 |
| **觸發時機** | IDE 掃描 `frontend/node_modules` 排除規則時讀取未註冊的 Registry key |
| **與專案程式無關** | 不是你的 Java / Vue 程式寫錯，是 IDE 內部 bug |
| **常見加劇因素** | PowerShell 外掛、SpotBugs 等舊外掛在關閉編輯器時連鎖觸發 |

此錯誤**通常不會阻止** `gradlew bootRun` 或 Terminal 啟動，但可能讓 IDE 關閉專案時噴 stack trace、索引變慢。

### 解法（依優先順序）

#### 解法 A：更新 IDE 與外掛（推薦）

```
Help → Check for Updates          （更新 IntelliJ IDEA 本體）
Settings → Plugins → Installed
  → 更新「JavaScript and TypeScript」
  → 更新「PowerShell」（若已安裝）
重啟 IDE
```

#### 解法 B：手動寫入 Registry 預設值（本專案腳本）

```powershell
cd D:\ClaudeCode\TradingCRUD
.\scripts\fix-intellij-registry.ps1
# 完全關閉 IntelliJ 後重新開啟
```

腳本會在 `%APPDATA%\JetBrains\IntelliJIdea*\options\registry.xml` 加入：

```xml
<entry key="node_modules.use.workspace.model" value="false" />
```

#### 解法 C：手動排除 node_modules（專案層）

```
1. cd frontend && npm install        （若尚未安裝）
2. IntelliJ Project 視窗 → 右鍵 frontend/node_modules
3. Mark Directory as → Excluded
```

同樣排除（若存在）：

- `server/node_modules`
- `build/`
- `.gradle/`

#### 解法 D：前後端分開開啟（最穩）

| 視窗 | 開啟路徑 | 用途 |
|------|----------|------|
| IDEA 1 | `D:\ClaudeCode\TradingCRUD` | Spring Boot 後端 |
| IDEA 2 或 VS Code | `D:\ClaudeCode\TradingCRUD\frontend` | Vue 前端 |

後端 Run Config 用 `TradingCrudApplication`；前端在另一視窗 `npm run dev`。

#### 解法 E：停用問題外掛（最後手段）

若更新後仍出現，暫時停用：

- PowerShell（stack trace 常見於關閉 `.ps1` 編輯器）
- SpotBugs（已知會觸發同一 Registry 讀取路徑）

### 驗證是否修復

```
1. 重啟 IntelliJ
2. Open D:\ClaudeCode\TradingCRUD
3. Gradle Sync
4. File → Close Project（不應再噴 MissingResourceException）
5. Run → TradingCrudApplication → http://localhost:8083/actuator/health
```

### 與 Gradle / settings.gradle 的關係

**無關。** 此錯誤發生在 IDE 的 JavaScript 索引層，不影響：

```powershell
.\gradlew.bat bootRun    # 仍正常
.\scripts\check.ps1      # 仍正常
```

---

*最後更新：2026-07-09 | 適用：IntelliJ IDEA 2023+ · Gradle 8.x · Spring Boot 3.x · JDK 21*
