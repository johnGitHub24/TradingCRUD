# TradingCRUD 架構（Spring Boot + Vue + Node）

> 分層哲學、關鍵類別、前後端整合。權威契約以 [`TradingCRUD 規格書.md`](TradingCRUD%20規格書.md) 為準。

---

## ① 系統總覽

```text
            ┌──────────────────────┐
            │   Browser (Vue 3)    │
            └─────────┬────────────┘
                      │
         ┌────────────┴────────────┐
         │ dev:5173    prod:3000   │
         ▼                         ▼
    Vite Proxy              Node.js BFF
         │                         │
         └────────────┬────────────┘
                      ▼
        ┌─────────────────────────────┐
        │   Spring Boot (:8083)       │
        │   Security + JWT + JPA      │
        └─────────────┬───────────────┘
                      ▼
              ┌───────────────┐
              │ H2 / PostgreSQL│
              └───────────────┘
```

---

## ② 後端分層

| 套件 | 職責 | 代表類別 |
|------|------|----------|
| `auth` | 登入 API | AuthController、AuthService |
| `order` | 訂單 CRUD/BATCH | OrderController、OrderService |
| `security` | JWT、Filter | JwtService、JwtAuthenticationFilter |
| `user` | 使用者 Entity | UserEntity、UserRepository |
| `config` | 安全、種子、CORS | SecurityConfig、DataSeeder、WebConfig |
| `common` | 例外、錯誤碼 | GlobalExceptionHandler、ErrorCodes |

**薄 Controller 原則：** Controller 只做 HTTP 轉換，商業邏輯在 Service。

---

## ③ 前端分層

| 目錄 | 職責 |
|------|------|
| `src/views/` | 頁面（Login、Orders） |
| `src/api/` | Axios 封裝 + JWT 攔截器 |
| `src/stores/` | 認證狀態（localStorage） |
| `src/router/` | 路由 + 登入守衛 |

---

## ④ 驗證與安全

| 層 | 機制 | 文件 |
|----|------|------|
| 傳輸 | JWT Bearer + @PreAuthorize | docs/驗證設計.md §2 |
| 格式 | @Valid on DTO | docs/驗證設計.md §3 |
| 業務 | Service 規則 | docs/驗證設計.md §4 |
| 資料 | UNIQUE 約束 | docs/資料庫設計.md |

---

## ⑤ 與 APIGatewayMQ 的差異

| 項目 | APIGatewayMQ | TradingCRUD |
|------|--------------|-------------|
| 下單 | 202 非同步 + Kafka | 201 同步 CRUD |
| 入口 | Gateway + 限流 | 直接 Spring Boot |
| 認證 | 無 | JWT + 角色 |
| 前端 | 無 | Vue 3 + Node BFF |
| 焦點 | 高併發削峰 | CRUD + 認證教學 |

---

*最後更新：2026-07-09*
