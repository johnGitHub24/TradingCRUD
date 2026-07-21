/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TradingCRUD Node.js BFF（Backend for Frontend）伺服器 — 主程式
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * 【什麼是 BFF？】
 *   BFF 是「專門為前端服務的後端」—— 瀏覽器不直接連 Spring Boot，
 *   而是先連這台 Node.js 伺服器，再由它轉發 API 請求、提供靜態網頁。
 *
 * 【整體架構（三層）】
 *
 *   瀏覽器 (:3000)
 *       │
 *       ▼
 *   Node.js BFF（本檔案）── 靜態檔 + API 代理
 *       │
 *       ├── /api/*  →  proxy 轉發 → Spring Boot (:8083)
 *       └── /*      →  回傳 Vue build 的 index.html
 *
 * 【為什麼需要 BFF？三個常見理由】
 *   1. 統一入口：使用者只記 localhost:3000，不必知道後端在 :8083
 *   2. 解決跨域（CORS）：瀏覽器與 API 都是 localhost:3000，視為「同源」
 *   3. SPA 路由：Vue Router 的 /orders 等路徑不是實體檔案，需 fallback 到 index.html
 *
 * 【何時啟動本伺服器？】
 *   - 正式部署或整合測試時（前端已 npm run build）
 *   - 開發時若要用 Vite 熱更新，請改跑 scripts/start-frontend.ps1（:5173）
 *
 * 【啟動步驟】
 *   1. cd frontend && npm run build   （產生靜態檔到 server/public）
 *   2. cd server && npm start           （啟動本伺服器 :3000）
 *   3. 確保 Spring Boot 已在 :8083 運行（scripts/start.ps1 或 gradlew bootRun）
 *
 * 【環境變數】
 *   PORT        — BFF 監聽埠，預設 3000
 *   API_TARGET  — Spring Boot 位址，預設 http://localhost:8083
 */

// ── 匯入第三方套件 ──────────────────────────────────────────────────────────
// express：建立 HTTP 伺服器、定義路由、提供中介層（middleware）
import express from 'express';
// cors：允許跨來源請求（開發時若直接連 :3000 可能需要）
import cors from 'cors';
// path：處理檔案路徑（跨 Windows / Linux 的斜線問題）
import path from 'path';
// fileURLToPath：ESM 模組沒有內建 __dirname，需從 import.meta.url 換算目錄
import { fileURLToPath } from 'url';
// http-proxy-middleware：把 /api 請求「轉發」到 Spring Boot，實作反向代理
import { createProxyMiddleware } from 'http-proxy-middleware';

/**
 * 【ESM 取得目前檔案目錄】
 * 傳統 CommonJS 有 __dirname；ESM（"type": "module"）需手動換算。
 * 用途：組出 server/public 的絕對路徑，不受「從哪個目錄執行 node」影響。
 */
const __dirname = path.dirname(fileURLToPath(import.meta.url));

/**
 * 【設定：監聽埠】
 * process.env.PORT — 作業系統環境變數；未設定時用 3000。
 * 部署到 Docker / 雲端時常由平台注入 PORT。
 */
const PORT = process.env.PORT || 3000;

/**
 * 【設定：後端 API 目標位址】
 * 所有 /api/* 請求會被 proxy 轉發到此 URL。
 * Docker 內可把 API_TARGET 設成 http://backend:8083 等服務名稱。
 */
const API_TARGET = process.env.API_TARGET || 'http://localhost:8083';

// 建立 Express 應用實例（app 上可掛中介層與路由）
const app = express();

/**
 * ═══ 中介層 1：CORS ═══
 *
 * 【什麼】允許其他網域的瀏覽器呼叫此伺服器。
 * 【為什麼】開發時若前端不在 :3000，沒有 CORS 會被瀏覽器擋下。
 * 【正式環境】前後端同源（都是 :3000）時通常不會觸發 CORS 檢查。
 */
app.use(cors());

/**
 * ═══ 中介層 2：API 反向代理（BFF 核心） ═══
 *
 * 【什麼】凡路徑以 /api 開頭的請求，轉發到 Spring Boot，不回傳本機靜態檔。
 *
 * 【運作範例】
 *   瀏覽器 GET  http://localhost:3000/api/v1/orders
 *     → proxy → http://localhost:8083/api/v1/orders
 *   瀏覽器 POST http://localhost:3000/api/v1/auth/login
 *     → proxy → http://localhost:8083/api/v1/auth/login
 *
 * 【參數說明】
 *   target       — 轉發目標（Spring Boot）
 *   changeOrigin — 改寫請求的 Host header，避免後端因 Host 不符而拒絕
 *   logLevel     — 僅在警告時印 log，減少終端機雜訊
 *
 * 【注意】此中介層必須在 SPA fallback（app.get('*')）之前註冊，
 *         否則 /api 可能被當成一般頁面而回傳 index.html。
 */
app.use('/api', createProxyMiddleware({
  target: API_TARGET,
  changeOrigin: true,
  logLevel: 'warn'
}));

/**
 * ═══ 中介層 3：靜態檔案服務 ═══
 *
 * 【什麼】直接提供磁碟上的 JS、CSS、圖片、index.html 等，不需經過 Spring Boot。
 * 【路徑】server/public/ — 由 frontend/vite.config.js 的 build.outDir 指定輸出位置。
 * 【何時生效】使用者請求 /assets/xxx.js、/favicon.ico 等實體檔案時。
 */
const publicDir = path.join(__dirname, 'public');
app.use(express.static(publicDir));

/**
 * ═══ 路由 4：SPA Fallback（單頁應用兜底） ═══
 *
 * 【什麼】上面都沒匹配到的 GET 請求，一律回傳 index.html。
 *
 * 【為什麼需要？】
 *   Vue Router 使用 History 模式，網址如 /orders、/login 不是實體檔案。
 *   使用者直接開啟或重新整理 /orders 時，Express 在 public/ 找不到 orders 檔案；
 *   若不回傳 index.html，瀏覽器會看到 404。
 *   回傳 index.html 後，Vue 載入並由前端路由渲染正確頁面。
 *
 * 【順序很重要】必須在 /api proxy 之後，避免 API 被誤當成 SPA 路由。
 */
app.get('*', (req, res) => {
  res.sendFile(path.join(publicDir, 'index.html'));
});

/**
 * 【啟動監聽】
 * app.listen 開始在 PORT 上接受 HTTP 連線。
 * 成功後在終端機印出位址，方便確認 proxy 目標是否正確。
 */
app.listen(PORT, () => {
  console.log(`TradingCRUD BFF running at http://localhost:${PORT}`);
  console.log(`API proxy → ${API_TARGET}`);
});
