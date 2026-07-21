# H2 Console 登入範本（公版）

> **用途：** 複製到任何 Spring Boot + H2 in-memory 專案。  
> **前置：** 後端已啟動（`{{MAIN_CLASS}}` / `bootRun`）。  
> **Console 網址：** http://localhost:{{BACKEND_PORT}}/h2-console

---

## 1. 專案變數（與主範本一致）

| 佔位符 | TradingCRUD 範例 | 你的專案 |
|--------|------------------|----------|
| `{{BACKEND_PORT}}` | 8083 | __________ |
| `{{H2_JDBC}}` | `jdbc:h2:mem:tradingcrud` | __________ |
| `{{H2_USER}}` | `sa` | __________ |
| `{{H2_PASSWORD}}` | （空白） | __________ |
| `{{TABLE_USERS}}` | `app_users` | __________ |
| `{{TABLE_MAIN}}` | `orders` | __________ |

> `{{H2_JDBC}}` 必須與 `application.yml` 的 `spring.datasource.url` **完全一致**（含 `mem:` 與資料庫名稱）。

---

## 2. 登入表單（照填）

| 欄位 | 填入值 |
|------|--------|
| Saved Settings | Generic H2 (Embedded)（可選，**JDBC URL 一定要手動改**） |
| Driver Class | `org.h2.Driver` |
| **JDBC URL** | `{{H2_JDBC}}` |
| **User Name** | `{{H2_USER}}` |
| **Password** | **留空**（不要填任何字） |

點 **Connect**。

---

## 3. 常見錯誤 90149

```
Database "C:/Users/xxx/test" not found ... [90149-224]
```

| 原因 | 說明 |
|------|------|
| JDBC URL 用預設 `jdbc:h2:~/test` | H2 會去找本機檔案 `C:/Users/你/test`，與 Spring Boot 無關 |
| URL 與 `application.yml` 不一致 | 連到**另一個**空的 in-memory DB，表是空的 |

**修正：** 刪掉錯誤 URL，改填 `{{H2_JDBC}}`（例：`jdbc:h2:mem:tradingcrud`）。

### 不要用的 JDBC URL

| 錯誤範例 | 結果 |
|----------|------|
| `jdbc:h2:~/test` | 90149 檔案庫不存在 |
| `jdbc:h2:mem:testdb` | 連到別的 mem DB，看不到應用資料 |
| `jdbc:h2:file:./data/mydb` | 檔案庫，除非專案刻意設定 |

---

## 4. `application.yml` 對照（新專案必核對）

```yaml
spring:
  datasource:
    url: {{H2_JDBC}}          # 例：jdbc:h2:mem:tradingcrud;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: {{H2_USER}}     # 例：sa
    password: {{H2_PASSWORD}} # 例：空白
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
```

**Security 公開路徑**（若 401 進不去 Console）：

```java
private static final String[] PUBLIC_PATHS = {
    // ...
    "/h2-console/**"
};
```

並確認 `headers.frameOptions(sameOrigin)`（Spring Security 預設已處理 iframe）。

---

## 5. 登入後驗證 SQL

複製 `files/h2-console-驗證.sql.template`，替換表名後在 H2 Console 執行：

```sql
-- 列出所有表
SHOW TABLES;

-- 使用者表
SELECT id, username, role, enabled, created_at FROM {{TABLE_USERS}};

-- 主業務表（依專案調整）
SELECT * FROM {{TABLE_MAIN}} ORDER BY id DESC;

-- 筆數
SELECT COUNT(*) AS cnt FROM {{TABLE_MAIN}};
```

**TradingCRUD 範例：**

```sql
SELECT id, username, role, enabled FROM app_users;
SELECT id, client_order_id, symbol, status FROM orders ORDER BY id DESC;
```

---

## 6. 兩種「登入」不要搞混

| 用途 | 網址 | 帳密 |
|------|------|------|
| **前台 Vue 應用** | `{{LOGIN_URL}}` | `{{DEFAULT_USER}}` / `{{DEFAULT_PASS}}` |
| **H2 Console 資料庫** | `http://localhost:{{BACKEND_PORT}}/h2-console` | `sa` / **空白** |

`app_users.password_hash` 是 **BCrypt 雜湊**，不會是明文密碼。

---

## 7. 其他注意事項

| 項目 | 說明 |
|------|------|
| in-memory 生命週期 | 關閉後端 → 資料清空；重啟後需重新 seed / 操作 |
| 後端必須運行中 | H2 mem 與 JVM 同一程序，關 IntelliJ Run 就連不到 |
| 表名大小寫 | H2 Console 左側可能顯示大寫 `APP_USERS`，SQL 用小寫通常也可 |

---

## 8. 新專案套用檢查

- [ ] `application.yml` 的 `spring.datasource.url` 已記下 → 填為 `{{H2_JDBC}}`
- [ ] `StartupInfoLogger` / `start.ps1` 有印 H2 URL 與 JDBC
- [ ] H2 Console Connect 成功
- [ ] `SHOW TABLES` 看得到預期資料表
- [ ] 前台操作後 `{{TABLE_MAIN}}` 有資料

---

*範本版本：2026-07-09 · 與 `SpringBoot-Vue-IntelliJ-啟動設定範本.md` 搭配使用*
