# TradingCRUD 測試規格書

> 本文件為 TradingCRUD 專案的測試主規格。
> 測試方法論參考 `APIGatewayMQ 規格書.md`（第 6 章）與 houseHub 測試方法論：
> fixture 分層、Case ID 命名、三層測試分工、DoD 檢查清單。

---

## 1. 系統範圍

| # | 功能 | 說明 |
|---|------|------|
| 1 | JWT 認證登入 | `POST /api/v1/auth/login`，admin 帳號取得 Bearer Token |
| 2 | 授權控管 | 未帶 Token → 401；非 ADMIN 寫入 → 403 |
| 3 | Order CRUD | 單筆新增 / 查詢 / 列表分頁 / 更新 / 刪除 |
| 4 | Order BATCH | 批次新增、批次刪除（部分成功回 207 Multi-Status） |

### 技術棧

| 層 | 技術 |
|----|------|
| 語言 | Java 21 |
| 框架 | Spring Boot 3.2.2、Spring MVC、Spring Security 6、JPA |
| 認證 | JWT（jjwt 0.12.6，HS256） |
| 資料庫 | H2（測試 / 本機）、PostgreSQL（正式） |
| 測試 | JUnit 5、MockMvc、Spring Security Test |
| 建置 | Gradle（`test` + `integrationTest`） |

---

## 2. 測試分層

| 層級 | Tag | Gradle 任務 | 需 DB | 說明 |
|------|-----|-------------|-------|------|
| 單元測試 | `@Tag("unit")` | `gradlew test` | 否 | JwtService、DTO 驗證、Service（Mock Repository） |
| 整合測試 | `@Tag("integration")` | `gradlew integrationTest` | H2 | 全流程 MockMvc + Security Filter Chain |
| 全專案 | — | `gradlew check` | H2 | unit + integration |

---

## 3. Case ID 對照

命名慣例：`{模組}-{序號}-{語意}`

| Case ID | 層 | 模組 | 測試類別 | 預期 |
|---------|----|------|----------|------|
| JWT-001 | 雙層 | security | JwtServiceTest + AuthIntegrationTest | Token 可解析；/me 用 subject |
| JWT-002 | 雙層 | security | JwtServiceTest + SecurityIntegrationTest | 竄改／無效 Token → isValid=false／401 |
| JWT-003 | 雙層 | security | JwtServiceTest + SecurityIntegrationTest | 異密鑰 Token 無效 |
| DTO-001 | 雙層 | order | CreateOrderRequestValidationTest + ORDER-001 | 合法 request 無錯誤 |
| DTO-002 | 雙層 | order | CreateOrderRequestValidationTest + ORDER-003 | 缺 clientOrderId → 驗證錯誤 |
| DTO-003 | 雙層 | order | CreateOrderRequestValidationTest + ORDER-004 | quantity ≤ 0 → 驗證錯誤 |
| SVC-001 | 雙層 | order | OrderServiceTest + ORDER-006 | create 重複 → Duplicate／409 |
| SVC-002 | 雙層 | order | OrderServiceTest + BATCH-006 | batchCreate 部分重複 |
| AUTH-001 | 雙層 | auth | AuthServiceTest + integration.AuthIntegrationTest | 登入成功簽發 JWT |
| AUTH-002 | 雙層 | auth | AuthServiceTest + AuthIntegrationTest | 錯誤密碼 → BadCredentials／401 |
| AUTH-003 | 雙層 | auth | LoginRequestValidationTest + AuthIntegrationTest | 缺欄位 → 400 |
| AUTH-004 | 雙層 | auth | JwtServiceTest + AuthIntegrationTest | 帶 Token GET /me |
| AUTH-005 | 雙層 | auth | AuthIntegrationTest + SEC-001 | 無 Token GET /me → 401 |
| USER-001 | 雙層 | security | AppUserDetailsServiceTest + AUTH-001 | 載入 ADMIN |
| USER-002 | 雙層 | security | AppUserDetailsServiceTest + AUTH-002 | 查無使用者 |
| SEC-001 | 雙層 | security | SecurityIntegrationTest + AUTH-005 | 無 Token → 401 |
| SEC-002 | 雙層 | security | AppUserDetailsServiceTest + SecurityIntegrationTest | USER 寫入 → ROLE_USER／403 |
| SEC-003 | 雙層 | security | AppUserDetailsServiceTest + SecurityIntegrationTest | USER 讀取允許 |
| SEC-004 | 雙層 | security | SecurityIntegrationTest + JWT-002/003 | 無效 Token → 401 |
| ORDER-001 | 雙層 | order | OrderServiceTest + OrderCrudIntegrationTest | 新增 201 |
| ORDER-002 | 雙層 | order | OrderServiceTest + OrderCrudIntegrationTest | 查詢 200 |
| ORDER-003 | 雙層 | order | DTO + OrderCrudIntegrationTest | 缺必填 400 |
| ORDER-004 | 雙層 | order | DTO + OrderCrudIntegrationTest | 格式非法 400 |
| ORDER-005 | 雙層 | order | OrderServiceTest + OrderCrudIntegrationTest | 更新 200 |
| ORDER-006 | 雙層 | order | OrderServiceTest + OrderCrudIntegrationTest | 重複 409 |
| ORDER-007 | 雙層 | order | OrderServiceTest + OrderCrudIntegrationTest | 刪除 204／不存在拋錯 |
| ORDER-008 | 雙層 | order | OrderServiceTest + OrderCrudIntegrationTest | 列表分頁 |
| BATCH-001 | 雙層 | order | OrderServiceTest + OrderBatchIntegrationTest | 批次全成功 |
| BATCH-003 | 雙層 | order | OrderServiceTest + OrderBatchIntegrationTest | 空清單 |
| BATCH-006 | 雙層 | order | OrderServiceTest + OrderBatchIntegrationTest | 部分重複 207 |
| BATCH-007 | 雙層 | order | OrderServiceTest + OrderBatchIntegrationTest | 批次刪除混合 207 |
| BATCH-008 | 雙層 | order | OrderServiceTest + OrderBatchIntegrationTest | 批次刪除全成功 |

---

## 4. Fixture 目錄

```text
docs/test-data/
├── auth/
│   ├── AUTH-001-SUCCESS.json
│   ├── AUTH-002-BAD_CREDENTIALS.json
│   └── AUTH-003-MISSING_REQUIRED.json
├── order/
│   ├── ORDER-001-SUCCESS.json
│   ├── ORDER-003-MISSING_REQUIRED.json
│   ├── ORDER-004-INVALID_FORMAT.json
│   └── ORDER-UPDATE.json
└── batch/
    ├── BATCH-001-SUCCESS.json
    ├── BATCH-006-DUPLICATE.json
    └── BATCH-003-MISSING_REQUIRED.json
```

載入流程：`CrudTestFixtures.loadJson(category, caseId)` → 原始 JSON → MockMvc `content(...)`。

---

## 5. 每支 API 最低案例類型（houseHub 方法論）

| 後綴 | 用途 |
|------|------|
| 001-SUCCESS | 正向路徑 |
| 003-MISSING_REQUIRED | 缺必填欄位（400） |
| 004-INVALID_FORMAT | 格式/值域非法（400） |
| 005 / 006 | 業務拒絕 / 冪等重複（409） |
| 007 | 部分失敗 / 刪除後不殘留 |

---

## 6. DoD 檢查清單

- [x] `gradlew check` 全綠（unit + integration）
- [x] admin 登入取得 JWT，帶 Token 可存取受保護端點
- [x] 無 Token → 401；非 ADMIN 寫入 → 403
- [x] CRUD 五個動作皆有正向 + 錯誤路徑測試
- [x] 重複 clientOrderId 回 409，不產生第二筆
- [x] 批次部分失敗回 207 且統計正確（succeeded / failed）
- [x] 公開 Service 方法各 ≥1 單元測；API Happy + ≥1 錯誤整合
- [ ] H2 執行 `docs/sql/01-schema-verify.sql` 無異常（可選，需 bootRun）
- [ ] Vue 前端手動 CRUD 驗證

---

## 7. 驗證與資料庫文件

| 文件 | 內容 |
|------|------|
| [`docs/驗證設計.md`](驗證設計.md) | 四層驗證、錯誤碼、權限矩陣 |
| [`docs/資料庫設計.md`](資料庫設計.md) | ER、JPA 對照、SQL 驗證 SOP |
| [`docs/sql/01-schema-verify.sql`](sql/01-schema-verify.sql) | 結構驗證 |
| [`docs/sql/02-crud-verify.sql`](sql/02-crud-verify.sql) | CRUD 手動驗證 |
| [`docs/testing.md`](測試與CI.md) | 腳本與 CI 說明 |

---

*最後更新：2026-08-13 | 技術棧：Spring Boot 3 · Spring Security 6 · JWT · Vue 3 · JUnit 5*
