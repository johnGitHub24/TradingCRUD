# TradingCRUD 前端（Vue 3 + Vite）

## 檔案結構與運作說明

```text
frontend/
├── index.html          ← HTML 殼層，Vue 掛載到 <div id="app">
├── vite.config.js      ← Vite 設定：dev proxy、build 輸出路徑
├── package.json        ← 依賴與 npm scripts
└── src/
    ├── main.js         ← 應用進入點：createApp → use(router) → mount
    ├── App.vue         ← 根元件：導覽列 + router-view + Toast
    ├── api/client.js   ← Axios 封裝：JWT 攔截器 + CRUD API 函式
    ├── stores/auth.js  ← 認證狀態：token 存 localStorage
    ├── router/index.js ← 路由表 + 登入守衛
    └── views/
        ├── LoginView.vue   ← 登入表單 → POST /auth/login
        └── OrdersView.vue  ← 訂單 CRUD 主畫面
```

## npm scripts 說明

| 指令 | 用途 |
|------|------|
| `npm run dev` | 啟動 Vite 開發伺服器（:5173），API 自動 proxy 到 :8083 |
| `npm run build` | 打包靜態檔到 `../server/public`，供 Node BFF 服務 |
| `npm run preview` | 預覽 build 結果（本專案建議用 server/ 啟動） |

## 資料流（登入 → CRUD）

```text
LoginView
  → api/client.login()
  → POST /api/v1/auth/login（Vite proxy → Spring Boot :8083）
  → auth.setSession(JWT) 寫入 localStorage
  → router.push('/orders')

OrdersView
  → api/client.fetchOrders() 帶 Authorization: Bearer <token>
  → GET /api/v1/orders
  → 渲染表格；ADMIN 可新增/編輯/刪除
```

## 開發啟動

```powershell
# 終端 1：後端
cd D:\SouceDemo\RemoteSpringBoot\TradingCRUD
.\gradlew.bat bootRun

# 終端 2：前端
cd frontend
npm install
npm run dev
# 開啟 http://localhost:5173
```
