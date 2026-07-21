# TradingCRUD Node.js BFF 伺服器

## 運作說明

本目錄的 `server.js` 是 **BFF（Backend for Frontend）** 層，在正式環境擔任瀏覽器與 Spring Boot 之間的橋樑。

```text
瀏覽器 ──► Node BFF (:3000) ──► Spring Boot (:8083)
              │
              ├── /api/*  → 反向代理（http-proxy-middleware）
              └── /*      → 靜態檔（Vue build 產物在 public/）
```

## 為什麼需要這一層？

| 問題 | BFF 解法 |
|------|----------|
| 開發時前後端不同埠，有 CORS | 瀏覽器只連 :3000，API 由 proxy 轉發，同源無 CORS |
| Vue Router History 模式重新整理 404 | `app.get('*')` 回傳 index.html，SPA 路由正常 |
| 正式部署只需開一個埠 | 使用者連 :3000 即可，不需知道後端位址 |

## package.json 逐欄說明（JSON 無法寫註解，故集中於此）

> 檔案位置：`server/package.json`  
> JSON 格式不支援 `//` 註解，以下對應各欄位用途，方便初學者對照 `server.js`。

```json
{
  "name": "trading-crud-server",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "start": "node server.js",
    "dev": "node server.js"
  },
  "dependencies": {
    "cors": "^2.8.5",
    "express": "^4.21.2",
    "http-proxy-middleware": "^3.0.3"
  }
}
```

| 欄位 | 值 | 說明 |
|------|-----|------|
| `name` | `trading-crud-server` | npm 套件名稱（本專案內部使用，不發佈到 npm 官網） |
| `version` | `0.1.0` | 語意化版本號 |
| `private` | `true` | 防止誤執行 `npm publish` 把程式推到公開 npm |
| `type` | `module` | 使用 **ESM** 語法（`import` / `export`），對應 `server.js` 第一行的 `import express` |
| `scripts.start` | `node server.js` | 正式啟動指令：`npm start` → 執行本目錄的 `server.js` |
| `scripts.dev` | `node server.js` | 開發用別名，行為與 `start` 相同（BFF 無熱更新，熱更新請用 Vite） |

### dependencies 與 server.js 對照

| 套件 | 在 server.js 的用途 | 對應程式碼概念 |
|------|---------------------|----------------|
| `express` | 建立 HTTP 伺服器、`app.use()` 中介層、`app.listen()` | 整個 `app` 物件與路由鏈 |
| `cors` | 跨來源請求標頭 | `app.use(cors())` |
| `http-proxy-middleware` | 把 `/api` 轉發到 Spring Boot | `createProxyMiddleware({ target: API_TARGET })` |

### server.js 程式區塊速查（對照原始碼閱讀）

| 區塊 | 行為 | 初學者記憶點 |
|------|------|--------------|
| `import ...` | 載入套件 | 等同 Java 的 `import` |
| `const PORT / API_TARGET` | 讀環境變數或預設值 | 部署時改環境變數即可，不必改程式 |
| `app.use(cors())` | 全站允許 CORS | 開發除錯用；正式同源可視需求調整 |
| `app.use('/api', createProxyMiddleware(...))` | **BFF 核心**：API 代理 | 瀏覽器打 :3000，實際資料來自 :8083 |
| `app.use(express.static(publicDir))` | 提供 Vue build 的靜態檔 | 先 `npm run build` 才會有 `public/` |
| `app.get('*', ...)` | SPA fallback | 重新整理 `/orders` 不會 404 |
| `app.listen(PORT, ...)` | 開始監聽連線 | 看到終端機訊息代表啟動成功 |

## npm scripts 說明

| 指令 | 用途 |
|------|------|
| `npm start` | 啟動 Express 伺服器（預設 :3000） |
| `npm run dev` | 同 start（開發用） |

## 環境變數

| 變數 | 預設 | 說明 |
|------|------|------|
| `PORT` | 3000 | BFF 監聽埠 |
| `API_TARGET` | http://localhost:8083 | Spring Boot 後端位址 |

## 啟動步驟

```powershell
# 1. 先 build 前端（產生 public/ 靜態檔）
cd ..\frontend
npm install
npm run build

# 2. 啟動 BFF
cd ..\server
npm install
npm start
# 開啟 http://localhost:3000
```

## 與 Vite dev 模式的差異

| 模式 | 埠 | API 轉發 | 適用 |
|------|-----|----------|------|
| Vite dev (`npm run dev`) | 5173 | vite.config.js proxy | 前端開發、熱更新 |
| Node BFF (`npm start`) | 3000 | server.js proxy | 正式部署、整合測試 |
