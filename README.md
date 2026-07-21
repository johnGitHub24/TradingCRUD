# TradingCRUD

JWT 認證 + Order CRUD/BATCH + **Vue 3 前端** + **Node.js BFF** 完整教學專案。  
參考 APIGatewayMQ 文件體系與 houseHub 測試方法論實作。

## 技術棧

| 層 | 技術 |
|----|------|
| 後端 | Java 21 · Spring Boot 3 · Spring Security 6 · JPA |
| 認證 | JWT（jjwt，HS256 Bearer Token） |
| 資料庫 | H2（本機/測試）· PostgreSQL（正式） |
| 前端 | Vue 3 · Vite · Axios · Bootstrap 5 |
| BFF | Node.js · Express · http-proxy-middleware |
| 文件 | springdoc-openapi（Swagger）· JavaDoc |
| 測試 | JUnit 5 · MockMvc · integrationTest |

## 文件入口

| 文件 | 說明 |
|------|------|
| [TradingCRUD 規格書.md](TradingCRUD%20規格書.md) | **主規格書（權威）** |
| [API規格書.md](API規格書.md) | API 端點、錯誤碼 |
| [docs/architecture.md](docs/architecture.md) | 架構摘要（EOS；指向中文詳文） |
| [docs/codeGraphic.html](docs/codeGraphic.html) | Tab 式架構圖（JWT／Order／前端／套件） |
| [docs/testing.md](docs/testing.md) | 測試摘要（EOS；指向 Case／CI） |
| [docs/架構學習導引.md](docs/架構學習導引.md) | 分層理解與學習路線 |
| [docs/資料庫設計.md](docs/資料庫設計.md) | ER、JPA、SQL 驗證 |
| [docs/驗證設計.md](docs/驗證設計.md) | 四層驗證、權限矩陣 |
| [docs/測試規格書.md](docs/測試規格書.md) | Case ID 對照 |
| [docs/測試與CI.md](docs/測試與CI.md) | Gradle／腳本／CI |
| [`docs/TradingCRUD-完整學習手冊.html`](docs/TradingCRUD-完整學習手冊.html) | **一次讀完所有 docs/*.md（推薦）** |
| [`docs/專案引導教學.html`](docs/專案引導教學.html) | 互動架構圖 |
| [`docs/Vue與Nodejs技術介紹.md`](docs/Vue與Nodejs技術介紹.md) | **Vue 3 / Node.js / Vite 技術入門** |
| [`docs/Vue與Nodejs技術介紹.html`](docs/Vue與Nodejs技術介紹.html) | **Vue / Node 互動教學頁** |
| [`docs/前後端串接說明.md`](docs/前後端串接說明.md) | Vue ↔ Spring Boot API |
| [docs/IntelliJ-IDE-啟動設定.md](docs/IntelliJ-IDE-啟動設定.md) | **IDE 啟動後端+前端（排錯必讀）** |
| [docs/templates/](docs/templates/) | **可複用到其他專案的啟動/IDE/H2 設定範本** |
| [frontend/README.md](frontend/README.md) | Vue 前端說明 |
| [server/README.md](server/README.md) | Node BFF 說明 |
| [CLAUDE.md](CLAUDE.md) | AI／工程薄規則（繼承 EOS 0.1.4） |

## 架構

```text
Browser (Vue 3)
  ├── 開發 :5173 ──Vite proxy──► Spring Boot :8083
  └── 正式 :3000 ──Node BFF───► Spring Boot :8083
                                    │
                                    ▼
                              H2 / PostgreSQL
                              app_users · orders
```

## 快速開始

```powershell
. .\scripts\env.ps1

# 1. 驗證測試
.\scripts\check.ps1

# 2. 啟動後端
.\gradlew.bat bootRun

# 3. 啟動前端（新終端）
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

或一鍵：`.\scripts\start.ps1`

**預設帳號：** admin / admin123

## 驗證指令

| 指令 | 用途 |
|------|------|
| `.\scripts\check.ps1` | 單元 + 整合測試 |
| `.\scripts\verify-db.ps1` | API smoke（DB 讀寫，需後端） |
| `.\scripts\fix-intellij-registry.ps1` | 修復 node_modules Registry 錯誤 |

## API 與 Swagger

| 項目 | URL |
|------|-----|
| Swagger UI | http://localhost:8083/swagger-ui.html |
| OpenAPI JSON | http://localhost:8083/v3/api-docs |
| H2 Console | http://localhost:8083/h2-console |

## 專案結構

```text
TradingCRUD/
├── src/main/java/     Spring Boot 後端
├── frontend/          Vue 3 前端（含完整註解）
├── server/            Node.js BFF（含完整註解）
├── docs/
│   ├── 前後端串接說明.md
│   ├── 專案引導教學.html
│   ├── 資料庫設計.md
│   ├── 驗證設計.md
│   ├── sql/           SQL 驗證腳本
│   └── test-data/     測試 fixture
└── scripts/           check · start · verify-db
```

---

*技術棧：Java 21 · Spring Boot 3 · Vue 3 · Node.js · JWT · JUnit 5*

## Document index (EOS)

| File | Description |
|------|-------------|
| [API規格書.md](API規格書.md) | **Master spec (authority)** |
| [docs/architecture.md](docs/architecture.md) | Architecture |
| [docs/testing.md](docs/testing.md) | Test / DoD |
| [CLAUDE.md](CLAUDE.md) | Thin AI rules (EOS) |

> Docs standard: EngineeringOS eos-minimal/knowledge/documentation.md
