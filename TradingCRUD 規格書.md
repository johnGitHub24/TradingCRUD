# TradingCRUD 規格書

> **本文件為 TradingCRUD 的唯一主規格書。**  
> 涵蓋：架構、資料庫、API 契約、JWT 認證、驗證、測試、前後端整合、驗收標準。

---

## 第 0 章　文件體系

| 文件 | 用途 |
|------|------|
| **`TradingCRUD 規格書.md`** | **主規格書（本文件）** |
| `TradingCRUD 架構（Spring Boot）.md` | 分層哲學、關鍵類別 |
| `API規格書.md` | API 端點完整參考 |
| `docs/architecture.md` | 架構摘要（EOS；指向中文詳文） |
| `docs/testing.md` | 測試摘要（EOS；指向 Case／CI） |
| `docs/專案引導教學.html` | 互動架構圖、前後端對照 |
| `docs/前後端串接說明.md` | Vue ↔ Spring Boot API 串接 |
| `docs/架構學習導引.md` | 學習路線 |
| `docs/初學者學習說明書.md` | 第一次跑起來 |
| `docs/功能流程說明.md` | 每個 API 怎麼跑 |
| `docs/資料庫設計.md` | ER、JPA、SQL 驗證 |
| `docs/驗證設計.md` | 四層驗證、錯誤碼 |
| `docs/測試規格書.md` | Case ID 對照 |
| `docs/測試與CI.md` | 腳本、DoD |
| `frontend/README.md` | Vue 前端說明 |
| `server/README.md` | Node BFF 說明 |
| `CLAUDE.md` | AI 薄規則（EOS 0.1.4） |

### 參考來源

| 來源 | 採用 | 不採用 |
|------|------|--------|
| APIGatewayMQ | 文件結構、測試方法論 | Kafka、Gateway、非同步下單 |
| houseHub | fixture、Case ID、三層測試 | Grails API、000000 錯誤碼 |
| TransactionClosedStateMachine | JavaDoc 註解風格 | 狀態機業務 |

---

## 第 1 章　系統範圍

### 1.1 核心功能

| # | 功能 | 說明 |
|---|------|------|
| 1 | JWT 登入 | POST `/api/v1/auth/login`，BCrypt + HS256 |
| 2 | 角色授權 | ADMIN 寫入、USER 唯讀 |
| 3 | Order CRUD | 單筆新增/查詢/列表/更新/刪除 |
| 4 | Order BATCH | 批次新增/刪除，部分失敗 207 |
| 5 | Vue 前端 | 登入 + 訂單管理 UI |
| 6 | Node BFF | 正式環境統一入口 :3000 |
| 7 | Swagger | springdoc-openapi + JavaDoc |

### 1.2 不在範圍

- Kafka / 訊息佇列
- 分散式事務
- 多租戶

### 1.3 技術棧

| 層 | 技術 |
|----|------|
| 後端 | Java 21、Spring Boot 3.2、Spring Security 6、JPA |
| 認證 | JWT（jjwt 0.12.6） |
| 資料庫 | H2（dev/test）、PostgreSQL（prod） |
| 前端 | Vue 3、Vite、Axios、Bootstrap 5 |
| BFF | Node.js、Express、http-proxy-middleware |
| 文件 | springdoc-openapi、JavaDoc |
| 測試 | JUnit 5、MockMvc、Spring Security Test |

---

## 第 2 章　架構

```text
瀏覽器
  │
  ├── 開發：Vite :5173 ──proxy /api──► Spring Boot :8083
  │
  └── 正式：Node BFF :3000 ──proxy /api──► Spring Boot :8083
                │
                └── 靜態檔（Vue build）

Spring Boot
  ├── auth/      AuthController、AuthService
  ├── order/     OrderController、OrderService、OrderBatchController
  ├── security/  JwtService、JwtAuthenticationFilter
  ├── user/      UserEntity、UserRepository
  └── config/    SecurityConfig、DataSeeder、OpenApiConfig
```

---

## 第 3 章　資料庫

詳見 [`docs/資料庫設計.md`](docs/資料庫設計.md)。

| 表 | 用途 |
|----|------|
| app_users | 使用者（BCrypt 密碼、角色） |
| orders | 訂單（client_order_id UNIQUE 冪等） |

---

## 第 4 章　API 契約

詳見 [`API規格書.md`](API規格書.md)。

Base URL：`http://localhost:8083/api/v1`

---

## 第 5 章　驗證

詳見 [`docs/驗證設計.md`](docs/驗證設計.md)。

四層：傳輸（JWT）→ 格式（@Valid）→ 業務（Service）→ 資料（UNIQUE）

---

## 第 6 章　測試

詳見 [`docs/測試規格書.md`](docs/測試規格書.md)。

```powershell
.\scripts\check.ps1          # 自動化測試
.\scripts\verify-db.ps1      # DB smoke（需後端）
```

---

## 第 7 章　驗收標準（DoD）

| # | 項目 | 驗證方式 |
|---|------|----------|
| 1 | 後端測試全綠 | `.\scripts\check.ps1` |
| 2 | 登入取得 JWT | Swagger 或 verify-db.ps1 |
| 3 | CRUD 五動作 | 整合測試 ORDER-001~008 |
| 4 | 批次 207 | BATCH-006/007 |
| 5 | DB 結構正確 | docs/sql/01-schema-verify.sql |
| 6 | 前端可操作 | Vue 手動或 dev 模式 |
| 7 | Swagger 可用 | /swagger-ui.html |
| 8 | JavaDoc 可產生 | `.\gradlew.bat javadoc` |

---

*最後更新：2026-07-09*
