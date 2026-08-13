# Vue.js 與 Node.js 技術介紹與使用指南

> **適用專案：** TradingCRUD  
> **讀者：** 具備基本 HTML/CSS/JavaScript，想理解本專案前端與 BFF 的初學者  
> **相關文件：** [Vue與Nodejs技術介紹.html](Vue與Nodejs技術介紹.html)（互動版） · [前後端串接說明.md](前後端串接說明.md) · [初學者學習說明書.md](初學者學習說明書.md) · [frontend/README.md](../frontend/README.md) · [server/README.md](../server/README.md)

---

## 1. 本專案前端技術棧一覽

| 技術 | 版本 | 角色 | 在本專案的位置 |
|------|------|------|----------------|
| **Vue 3** | 3.5.x | UI 框架（畫面、互動） | `frontend/src/` |
| **Vue Router** | 4.5.x | 單頁應用路由（/login、/orders） | `frontend/src/router/` |
| **Vite** | 6.x | 開發伺服器、打包工具 | `frontend/vite.config.js` |
| **Axios** | 1.7.x | HTTP 客戶端（呼叫後端 API） | `frontend/src/api/client.js` |
| **Bootstrap 5** | 5.3（CDN） | UI 樣式（表格、按鈕、Modal） | `frontend/index.html` |
| **Node.js** | 18+ | JavaScript 執行環境 | 跑 Vite、跑 BFF |
| **Express** | 4.x | Node 網頁框架（BFF） | `server/server.js` |
| **http-proxy-middleware** | 3.x | API 反向代理 | `server/server.js` |

```text
【開發模式 — 日常寫程式用】
  瀏覽器 :5173
      ↓  Vue 3 + Vite（熱更新 HMR）
      ↓  /api/* 由 Vite proxy 轉到 :8083
  Spring Boot :8083

【正式/整合模式 — 模擬上線】
  瀏覽器 :3000
      ↓  Node.js Express BFF（靜態檔 + proxy）
      ↓  /api/* 轉到 :8083
  Spring Boot :8083
```

---

## 2. Vue.js 是什麼？

**Vue.js** 是一套用來建構使用者介面（UI）的 **JavaScript 框架**。  
核心想法：**資料驅動畫面**——你改資料，畫面自動更新，不必手動操作 DOM。

### 2.1 與傳統 HTML/JS 的差異

| 傳統做法 | Vue 做法 |
|----------|----------|
| `document.getElementById` 改文字 | `{{ username }}` 綁定變數 |
| 手動建立/刪除表格列 | `v-for` 遍歷陣列自動渲染 |
| 自己組登入後跳頁邏輯 | Vue Router 管理網址與頁面 |
| 到處複製貼上 fetch 程式碼 | `api/client.js` 集中封裝 |

### 2.2 本專案用到的 Vue 3 核心概念

#### （1）單檔元件 `.vue`

一個檔案包含三塊（本專案主要用 template + script）：

```vue
<template>
  <!-- HTML 樣板：畫面長什麼樣 -->
</template>

<script setup>
  // JavaScript 邏輯：資料與方法
</script>

<style scoped>
  /* 選用：此元件專用樣式 */
</style>
```

範例檔案：`frontend/src/views/LoginView.vue`、`OrdersView.vue`

#### （2）Composition API 與 `ref`

```javascript
import { ref } from 'vue';

const username = ref('admin');  // 響應式變數
const loading = ref(false);

// script 裡讀寫要 .value
username.value = 'test';

// template 裡直接用 username（自動解包）
```

**學習重點：** `ref` 包住的值在 `<script>` 要用 `.value`，在 `<template>` 不用。

#### （3）`computed` 計算屬性

```javascript
import { computed } from 'vue';

const isLoggedIn = computed(() => !!state.token);
```

依賴的資料變了，計算結果自動更新。  
**陷阱：** 在 `router/index.js` 等普通 `.js` 檔要搭配 `unref()`，見 `frontend/src/stores/auth.js` 註解。

#### （4）生命週期 `onMounted`

```javascript
import { onMounted } from 'vue';

onMounted(() => {
  loadOrders();  // 元件掛載到畫面後執行
});
```

`OrdersView.vue` 用它在進入訂單頁時自動載入列表。

#### （5）模板指令（最常見）

| 指令 | 用途 | 本專案範例 |
|------|------|------------|
| `v-model` | 雙向綁定表單 | 登入帳密輸入框 |
| `v-if` / `v-else` | 條件顯示 | 僅 ADMIN 顯示新增按鈕 |
| `v-for` | 迴圈渲染 | 訂單表格每一列 |
| `@click` | 點擊事件 | 刪除、分頁按鈕 |
| `@submit.prevent` | 表單送出（阻止預設跳頁） | 登入表單 |

#### （6）Vue Router（前端路由）

瀏覽器網址變了，**不整頁重新載入**，只換中間的元件：

| 網址 | 元件 | 說明 |
|------|------|------|
| `/login` | `LoginView.vue` | 訪客頁，已登入會導走 |
| `/orders` | `OrdersView.vue` | 需登入 |
| `/` | 重新導向 `/orders` | 根路徑 |

路由守衛在 `frontend/src/router/index.js`：未登入不能進 `/orders`。

---

## 3. Vite 是什麼？怎麼用？

**Vite** 是前端的 **開發與打包工具**（類似 webpack，但開發時更快）。

### 3.1 開發模式 `npm run dev`

```powershell
cd frontend
npm install    # 第一次
npm run dev    # 啟動 http://localhost:5173
```

| 功能 | 說明 |
|------|------|
| **HMR 熱更新** | 改 `.vue` 存檔，瀏覽器幾乎即時更新，不丟狀態 |
| **ES Module** | 瀏覽器直接 `import`，啟動快 |
| **Proxy** | 把 `/api` 轉發到 Spring Boot，解決跨域 |

`vite.config.js` 關鍵設定：

```javascript
server: {
  port: 5173,
  open: '/login',           // 啟動自動開登入頁
  proxy: {
    '/api': {
      target: 'http://localhost:8083',  // 後端位址
      changeOrigin: true
    }
  }
}
```

**初學者理解 proxy：**  
瀏覽器以為 API 也是 `localhost:5173/api/...`，實際由 Vite 偷偷轉給 `8083`，所以不會遇到 CORS 擋下來。

### 3.2 正式打包 `npm run build`

```powershell
npm run build
```

產出靜態檔（HTML/JS/CSS）到 `server/public/`，給 Node BFF 或 Nginx 服務。

### 3.3 常用指令

| 指令 | 用途 |
|------|------|
| `npm run dev` | 開發（:5173） |
| `npm run build` | 打包 |
| `npm run preview` | 預覽打包結果 |

專案腳本：`cd frontend; npm run dev` 或 IntelliJ **Frontend (Vite)**。

---

## 4. Axios 與 API 層

**Axios** 是發 HTTP 請求的函式庫（比原生 `fetch` 攔截器更方便）。

本專案封裝在 `frontend/src/api/client.js`：

```text
View 層（LoginView / OrdersView）
        ↓ 呼叫 export function
api/client.js（axios 實例 + 攔截器）
        ↓  HTTP
Vite proxy 或 Node BFF
        ↓
Spring Boot REST API
```

### 4.1 Request 攔截器（自動帶 JWT）

每次請求前從 `auth` store 讀 token，加到 Header：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### 4.2 Response 攔截器（401 統一處理）

- Token 過期或無效 → 登出 → 導向 `/login`
- **登入 API 的 401 不觸發**（帳密錯誤由 LoginView 顯示訊息）

### 4.3 對外函式一覽

| 函式 | 說明 |
|------|------|
| `login(username, password)` | 登入，回傳 JWT |
| `fetchOrders(params)` | 分頁查詢訂單 |
| `createOrder(payload)` | 新增 |
| `updateOrder(id, payload)` | 更新 |
| `deleteOrder(id)` | 刪除 |
| `batchCreateOrders(orders)` | 批次新增 |
| `batchDeleteOrders(ids)` | 批次刪除 |

---

## 5. 認證狀態 `stores/auth.js`

輕量狀態管理（未用 Pinia，方便教學）：

| 欄位 | 存放處 | 用途 |
|------|--------|------|
| `token` | 記憶體 + `localStorage` | JWT |
| `username` | 同上 | 導覽列顯示 |
| `role` | 同上 | `ADMIN` 才能寫入 |

| 方法 | 時機 |
|------|------|
| `setSession({ accessToken, username, role })` | 登入成功 |
| `logout()` | 登出或 401 |

**重新整理頁面仍保持登入：** 因為 token 存在 `localStorage`。

---

## 6. Node.js 在本專案做什麼？

**Node.js** 讓 JavaScript 能在**電腦上當伺服器**執行（不只跑在瀏覽器）。

本專案 Node 有 **兩種用途**：

| 用途 | 怎麼跑 | 埠 |
|------|--------|-----|
| 跑 **Vite** 開發工具 | `npm run dev`（在 frontend/） | 5173 |
| 跑 **Express BFF** | `npm start`（在 server/） | 3000 |

### 6.1 什麼是 BFF（Backend for Frontend）？

`server/server.js` 扮演的角色：

```text
瀏覽器只連 localhost:3000
    │
    ├─ GET /orders        → 回傳 Vue 打包後的 index.html（SPA）
    ├─ GET /assets/*.js   → 回傳靜態 JS/CSS
    └─ /api/v1/*          → proxy 轉發到 Spring Boot :8083
```

**好處：**

1. **單一入口**：使用者只記一個網址  
2. **同源**：瀏覽器與 API 都是 `:3000`，減少 CORS 問題  
3. **SPA 支援**：Vue Router 的路徑由 `index.html` fallback 處理  

### 6.2 Express 中介層順序（server.js）

```text
1. cors()           — 允許跨來源（開發用）
2. express.json()   — 解析 JSON body
3. /api  proxy      — 轉發到 Spring Boot
4. express.static   — 服務 public/ 靜態檔
5. SPA fallback     — 其餘路徑回 index.html
```

### 6.3 啟動 BFF 完整步驟

```powershell
# 1. 後端
cd D:\SouceDemo\RemoteSpringBoot\TradingCRUD
.\gradlew.bat bootRun

# 2. 打包前端
cd frontend
npm run build

# 3. 啟動 BFF
cd ..\server
npm install
npm start
# → http://localhost:3000/login
```

### 6.4 server/ 依賴說明

| 套件 | 用途 |
|------|------|
| `express` | HTTP 伺服器、路由 |
| `cors` | 跨來源設定 |
| `http-proxy-middleware` | 把 `/api` 轉到 :8083 |

---

## 7. 專案目錄與閱讀順序

### 7.1 frontend/ 結構

```text
frontend/
├── index.html              # 網頁殼層
├── vite.config.js          # Vite + proxy
├── package.json            # 依賴與 scripts
└── src/
    ├── main.js             # ① 進入點
    ├── App.vue             # ② 根版面（導覽列、Toast）
    ├── router/index.js     # ③ 路由與守衛
    ├── stores/auth.js      # ④ 登入狀態
    ├── api/client.js       # ⑤ HTTP + JWT
    └── views/
        ├── LoginView.vue   # ⑥ 登入頁
        └── OrdersView.vue  # ⑦ 訂單 CRUD
```

### 7.2 建議學習路線（約 2～3 小時）

```
Step 1  讀 main.js、App.vue           → Vue 怎麼啟動
Step 2  讀 LoginView + auth.js        → 登入與狀態
Step 3  讀 client.js                  → 怎麼呼叫 API
Step 4  讀 router/index.js            → 路由守衛
Step 5  讀 OrdersView.vue             → 列表、Modal、CRUD
Step 6  讀 vite.config.js             → 開發 proxy
Step 7  讀 server/server.js           → 正式 BFF 模式
Step 8  對照 docs/前後端串接說明.md   → 與 Java API 對照
```

---

## 8. 完整資料流（登入 → 查訂單）

```mermaid
sequenceDiagram
    participant U as 使用者瀏覽器
    participant V as Vue (:5173)
    participant P as Vite Proxy
    participant S as Spring Boot (:8083)
    participant D as H2 資料庫

    U->>V: 開啟 /login，輸入帳密
    V->>P: POST /api/v1/auth/login
    P->>S: 轉發到 :8083
    S->>D: 驗證 app_users
    S-->>V: accessToken + role
    V->>V: auth.setSession() 寫 localStorage
    V->>V: router.push('/orders')
    V->>P: GET /api/v1/orders + Bearer Token
    P->>S: 轉發
    S->>D: SELECT orders
    S-->>V: JSON 列表
    V-->>U: 渲染表格
```

---

## 9. 常用網址與指令速查

### 9.1 網址

| 用途 | 開發 | 正式 BFF |
|------|------|----------|
| 登入頁 | http://localhost:5173/login | http://localhost:3000/login |
| 訂單頁 | http://localhost:5173/orders | http://localhost:3000/orders |
| 後端 API | http://localhost:8083/api/v1 | 同左（經 proxy） |
| Swagger | http://localhost:8083/swagger-ui.html | — |

### 9.2 指令

```powershell
# 一鍵後端+前端（開發）
.\gradlew.bat bootRun

# 只前端
cd frontend; npm run dev

# IntelliJ
Run → Full Stack
```

---

## 10. 常見問題（FAQ）

| 問題 | 原因 | 解法 |
|------|------|------|
| 登入後一直跳 Authentication required | Router 未 `unref(isLoggedIn)` | 見 `router/index.js`、`stores/auth.js` |
| `:5173` 連不上 | 前端沒啟動 | `npm run dev` 或 Full Stack |
| API 404 / Network Error | 後端沒跑 | `bootRun` 或 TradingCrudApplication |
| CORS 錯誤 | 繞過 proxy 直連 :8083 | 用相對路徑 `/api/v1`，走 Vite proxy |
| 改程式畫面沒變 | 沒存檔或跑錯目錄 | 確認 Vite 終端在跑、瀏覽器強制重新整理 |
| build 後 :3000 空白 | 沒先 `npm run build` | 先 build 再 `server/npm start` |

---

## 11. 延伸學習資源

| 主題 | 官方文件 |
|------|----------|
| Vue 3 | https://vuejs.org/guide/introduction.html |
| Vue Router | https://router.vuejs.org/ |
| Vite | https://vite.dev/guide/ |
| Axios | https://axios-http.com/docs/intro |
| Express | https://expressjs.com/ |
| Bootstrap 5 | https://getbootstrap.com/docs/5.3/getting-started/introduction/ |

---

## 12. 與後端（Java）的分工

| 層 | 技術 | 負責 |
|----|------|------|
| 畫面與互動 | Vue 3 | 表單、表格、路由、Token 存放 |
| 傳輸 | Vite / Node proxy | 轉發 `/api`，開發或正式入口 |
| 商業邏輯與安全 | Spring Boot | 驗證 JWT、CRUD、權限、資料庫 |
| 資料 | H2 / PostgreSQL | 持久化 |

**原則：** 前端做「體驗與路由」；**真正的授權與資料正確性在後端**。  
前端隱藏按鈕只是 UX，使用者仍可直接打 API，所以 Spring Security 必須擋住。

---

*最後更新：2026-07-09 · 專案：TradingCRUD*
