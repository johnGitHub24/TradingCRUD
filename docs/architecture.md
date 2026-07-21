# Architecture — TradingCRUD

> 衝突以 [TradingCRUD 規格書.md](../TradingCRUD%20規格書.md) 為準。  
> 本檔為 EOS 精簡入口；完整分層／學習路線見下方中文文件，**勿在此重複全文**。

## Layers

| Layer | Responsibility |
|-------|----------------|
| Frontend | Vue 3 + Vite（dev :5173） |
| BFF | Node Express（prod :3000，proxy `/api`） |
| Controller / API | HTTP、參數、`@Valid`、ResponseEntity |
| Service | 商業邏輯、`@Transactional` |
| Security | JWT Filter、角色 ADMIN／USER |
| Repository / Entity | JPA → H2（dev/test）／PostgreSQL（prod） |

## Module map

| Module | Notes |
|--------|-------|
| Auth | `AuthController` — `POST /api/v1/auth/login`、`GET /auth/me` |
| Order CRUD | `OrderController` — 單筆 CRUD |
| Order BATCH | 批次新增／刪除（部分失敗 207） |
| Frontend | `frontend/` Vue 訂單管理 UI |
| BFF | `server/` 正式靜態 + API proxy |

## Runtime

```text
Browser (Vue)
  ├── 開發 :5173 ──Vite proxy──► Spring Boot :8083
  └── 正式 :3000 ──Node BFF───► Spring Boot :8083
                                    │
                                    ▼
                              H2 / PostgreSQL
                              app_users · orders
```

## 詳細文件（請由此深入）

| 文件 | 說明 |
|------|------|
| [架構學習導引.md](架構學習導引.md) | 三層理解、學習路線、文件地圖 |
| [功能流程說明.md](功能流程說明.md) | 主要 API 流程 |
| [資料庫設計.md](資料庫設計.md) | ER、Entity、SQL 驗證 |
| [驗證設計.md](驗證設計.md) | 四層驗證、權限矩陣、錯誤碼 |
| [前後端串接說明.md](前後端串接說明.md) | Vue ↔ Spring Boot |
| [../API規格書.md](../API規格書.md) | 端點契約 |
| [../TradingCRUD 規格書.md](../TradingCRUD%20規格書.md) | 權威驗收 |

## Visual maps

| 文件 | 用途 |
|------|------|
| [codeGraphic.html](codeGraphic.html) | Tab：JWT／Order／前端／套件（圖為主） |
| [專案引導教學.html](專案引導教學.html) | 長文引導＋流程圖 |
