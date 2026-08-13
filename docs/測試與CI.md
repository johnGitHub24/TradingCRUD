# TradingCRUD 測試與 CI

> Case ID 對照、驗證腳本、CI 整合說明。  
> 完整 Case 列表見 [`docs/測試規格書.md`](測試規格書.md)。

---

## 1. 驗證指令速查

| 指令 | 用途 | 需後端運行 |
|------|------|-----------|
| `.\scripts\check.ps1` | 單元 + 整合測試（Gradle check） | 否 |
| `.\scripts\verify-db.ps1` | API smoke test（DB 讀寫） | 是 (:8083) |
| `.\scripts\export-openapi.ps1` | 匯出 OpenAPI JSON | 是 |
| `.\gradlew.bat javadoc` | 產生 JavaDoc | 否 |

---

## 2. Gradle 任務

```powershell
. .\scripts\env.ps1

.\gradlew.bat test              # 單元測試（@Tag unit 或無 tag）
.\gradlew.bat integrationTest   # 整合測試（@Tag integration）
.\gradlew.bat check             # 以上兩者
.\gradlew.bat bootRun           # 啟動後端
.\gradlew.bat javadoc           # JavaDoc → build/docs/javadoc/
```

---

## 3. 三層測試分工

| 層 | 測什麼 | 不測什麼 | 工具 |
|----|--------|----------|------|
| 單元 | JwtService、DTO @Valid、Service（Mock Repo） | HTTP、DB | JUnit 5 |
| 整合 | MockMvc 全流程 + H2 + Security Filter | 真實 PostgreSQL | @SpringBootTest |
| Smoke | 真實 HTTP + DB 讀寫 | 邊界條件全覆蓋 | verify-db.ps1 |

---

## 4. Fixture 載入

```java
// CrudTestFixtures.loadJson("auth", "AUTH-001-SUCCESS")
// → docs/test-data/auth/AUTH-001-SUCCESS.json
```

---

## 5. 前端驗證

```powershell
# 終端 1
.\gradlew.bat bootRun

# 終端 2
cd frontend
npm install
npm run dev
# 手動驗證：登入 → 新增訂單 → 編輯 → 刪除 → 批次操作
```

---

## 6. DoD（Definition of Done）

- [x] `gradlew check` 全綠
- [ ] `verify-db.ps1` 通過
- [ ] H2 SQL 驗證腳本執行無異常
- [ ] Swagger Authorize 後 CRUD 成功
- [ ] Vue 前端全流程手動驗證

---

*最後更新：2026-07-09*
