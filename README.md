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

單一入口：本 README。衝突以主規格為準。

| 文件 | 說明 |
|------|------|
| [TradingCRUD 規格書.md](TradingCRUD%20規格書.md) | **主規格（權威）** |
| [API規格書.md](API規格書.md) | API 契約 |
| [docs/architecture.md](docs/architecture.md) | 分層與模組 |
| [docs/codeGraphic.html](docs/codeGraphic.html) | 架構圖（非權威） |
| [docs/testing.md](docs/testing.md) | 測試／Case／check |
| [docs/Vue與Nodejs技術介紹.md](docs/Vue與Nodejs技術介紹.md) | 本專案前端 |
| [docs/前後端串接說明.md](docs/前後端串接說明.md) | 本專案前端 |
| [docs/資料庫設計.md](docs/資料庫設計.md) | 資料庫 |
| [docs/驗證設計.md](docs/驗證設計.md) | 驗證／權限 |
| [CLAUDE.md](CLAUDE.md) | AI 薄規則 |
| [scripts/README.md](scripts/README.md) | 驗證／啟動腳本 |
| [docs/swagger.html](docs/swagger.html) | **API（Swagger／介面）** |

### 教學（非權威，勿刪）

| 文件 | 說明 |
|------|------|
| [docs/專案引導教學.html](docs/專案引導教學.html) | 教學 |
| [docs/初學者學習說明書.md](docs/初學者學習說明書.md) | 教學 |
| [docs/架構學習導引.md](docs/架構學習導引.md) | 教學 |
| [docs/功能流程說明.md](docs/功能流程說明.md) | 教學 |
| [docs/TradingCRUD-完整學習手冊.html](docs/TradingCRUD-完整學習手冊.html) | 教學 |
| [docs/Vue與Nodejs技術介紹.md](docs/Vue與Nodejs技術介紹.md) | 教學 |
| [docs/前後端串接說明.md](docs/前後端串接說明.md) | 教學 |
| [docs/IntelliJ-IDE-啟動設定.md](docs/IntelliJ-IDE-啟動設定.md) | 教學 |

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

驗證後啟動：`.\scripts\check.ps1`，再 `.\gradlew.bat bootRun` 或 IntelliJ Gradle `bootRun`。

**預設帳號：** admin / admin123

## 驗證指令

| 指令 | 用途 |
|------|------|
| `.\scripts\check.ps1` | 單元 + 整合測試 |
| `.\gradlew.bat bootRun` | 啟動後端（或 IntelliJ Gradle bootRun） |

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
└── scripts/           check · env · portable-env · IntelliJ fix
```

---

*技術棧：Java 21 · Spring Boot 3 · Vue 3 · Node.js · JWT · JUnit 5*

> Docs standard: EngineeringOS eos-minimal/knowledge/documentation.md

