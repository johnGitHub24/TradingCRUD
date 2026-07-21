# API 規格書 — TradingCRUD

> API 契約完整參考。與 [`TradingCRUD 規格書.md`](TradingCRUD%20規格書.md) 互補；衝突時以主規格書為準。

---

## 0. 路由總覽

| # | Method | 路徑 | 權限 | 回應 | 說明 |
|---|--------|------|------|------|------|
| 1 | POST | `/api/v1/auth/login` | 公開 | 200 | 登入取得 JWT |
| 2 | GET | `/api/v1/auth/me` | 已登入 | 200 | 目前使用者 |
| 3 | POST | `/api/v1/orders` | ADMIN | 201 | 新增訂單 |
| 4 | GET | `/api/v1/orders/{id}` | 已登入 | 200 | 查詢單筆 |
| 5 | GET | `/api/v1/orders` | 已登入 | 200 | 列表分頁 |
| 6 | PUT | `/api/v1/orders/{id}` | ADMIN | 200 | 更新訂單 |
| 7 | DELETE | `/api/v1/orders/{id}` | ADMIN | 204 | 刪除訂單 |
| 8 | POST | `/api/v1/orders/batch` | ADMIN | 201/207 | 批次新增 |
| 9 | DELETE | `/api/v1/orders/batch` | ADMIN | 200/207 | 批次刪除 |

**Base URL**

| 環境 | URL |
|------|-----|
| 本機後端 | `http://localhost:8083` |
| Vite 前端（proxy） | `http://localhost:5173` |
| Node BFF | `http://localhost:3000` |

**OpenAPI / Swagger**

| 項目 | URL |
|------|-----|
| Swagger UI | http://localhost:8083/swagger-ui.html |
| OpenAPI JSON | http://localhost:8083/v3/api-docs |

---

## 1. 認證

### POST `/api/v1/auth/login`

#### Request

```json
{
  "username": "admin",
  "password": "admin123"
}
```

| 欄位 | 型別 | 必填 | 驗證 |
|------|------|------|------|
| username | string | 是 | @NotBlank |
| password | string | 是 | @NotBlank |

#### Response 200

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "username": "admin",
  "role": "ADMIN"
}
```

#### Response 401

```json
{
  "errorCode": "BAD_CREDENTIALS",
  "detail": "Invalid username or password"
}
```

---

## 2. 訂單 CRUD

### POST `/api/v1/orders`

**Headers：** `Authorization: Bearer <token>`

#### Request

```json
{
  "clientOrderId": "o-001",
  "symbol": "BTCUSDT",
  "side": "BUY",
  "quantity": 0.5,
  "price": 65000
}
```

| 欄位 | 型別 | 必填 | 約束 |
|------|------|------|------|
| clientOrderId | string | 是 | max 64，UNIQUE |
| symbol | string | 是 | max 20 |
| side | string | 是 | BUY / SELL |
| quantity | number | 是 | > 0 |
| price | number | 是 | > 0 |

#### Response 201

```json
{
  "id": 1,
  "clientOrderId": "o-001",
  "symbol": "BTCUSDT",
  "side": "BUY",
  "quantity": 0.5,
  "price": 65000,
  "status": "NEW",
  "createdAt": "2026-07-09T12:00:00+08:00",
  "updatedAt": "2026-07-09T12:00:00+08:00"
}
```

Header：`Location: /api/v1/orders/1`

#### Response 409

```json
{
  "errorCode": "DUPLICATE_ORDER",
  "detail": "clientOrderId already exists: o-001"
}
```

### GET `/api/v1/orders`

**Query：** `page`（預設 0）、`size`（預設 20，max 100）、`symbol`、`status`

#### Response 200

```json
{
  "data": [ { "id": 1, "clientOrderId": "o-001", "...": "..." } ],
  "meta": { "page": 0, "size": 20, "total": 1 }
}
```

### PUT `/api/v1/orders/{id}`

#### Request

```json
{
  "symbol": "ETHUSDT",
  "side": "SELL",
  "quantity": 2,
  "price": 3200,
  "status": "FILLED"
}
```

### DELETE `/api/v1/orders/{id}`

Response 204，無 body。

---

## 3. 批次操作

### POST `/api/v1/orders/batch`

```json
{
  "orders": [
    { "clientOrderId": "b-1", "symbol": "BTCUSDT", "side": "BUY", "quantity": 0.1, "price": 65000 },
    { "clientOrderId": "b-2", "symbol": "ETHUSDT", "side": "SELL", "quantity": 1, "price": 3200 }
  ]
}
```

- 全成功：201
- 部分失敗：207 + BatchResult

### DELETE `/api/v1/orders/batch`

```json
{ "ids": [1, 2, 3] }
```

---

## 4. 錯誤碼

| errorCode | HTTP | 說明 |
|-----------|------|------|
| VALIDATION_FAILED | 400 | @Valid 失敗 |
| BAD_CREDENTIALS | 401 | 帳密錯誤 |
| UNAUTHORIZED | 401 | 未帶/無效 Token |
| FORBIDDEN | 403 | 權限不足 |
| ORDER_NOT_FOUND | 404 | 訂單不存在 |
| DUPLICATE_ORDER | 409 | clientOrderId 重複 |
| INTERNAL_ERROR | 500 | 未預期錯誤 |

---

*最後更新：2026-07-09*
