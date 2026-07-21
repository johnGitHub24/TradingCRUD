/**
 * =============================================================================
 * vite.config.js — Vite 建置與開發伺服器設定檔
 * =============================================================================
 *
 * 【這個檔案是什麼？】
 * Vite 是 Vue 3 專案常用的「建置工具」（Build Tool），類似 Webpack 但開發時更快。
 * 此檔案告訴 Vite：如何啟動開發伺服器、如何編譯 .vue 檔、打包後輸出到哪裡。
 *
 * 【學習重點 — Vite 與 Vue 的關係】
 * - 開發時（npm run dev）：Vite 啟動本機伺服器，即時編譯 Vue 元件，支援熱更新（HMR）
 * - 打包時（npm run build）：將所有 .vue / .js 壓縮合併成靜態 HTML/CSS/JS
 * - defineConfig()：提供 TypeScript 型別提示（即使本專案用 .js 也建議使用）
 *
 * 【與後端的協作方式】
 * 前端開發埠 5173，後端 Spring Boot 埠 8083。
 * proxy 設定讓瀏覽器請求 /api/* 時，由 Vite 代轉到後端，避免跨域（CORS）問題。
 */
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  /**
   * plugins — 外掛清單
   * @vitejs/plugin-vue：讓 Vite 能解析 .vue 單檔元件（SFC = Single File Component）
   * 沒有這個外掛，import App from './App.vue' 會編譯失敗
   */
  plugins: [vue()],

  /**
   * server — 開發模式（npm run dev）專用設定
   * 正式環境不會用到這段，正式環境由 Node BFF 或 Nginx 服務打包後的靜態檔
   */
  server: {
    /** port: 5173 — Vite 預設埠，刻意與 Spring Boot :8083 分開，避免衝突 */
    port: 5173,

    /**
     * open: '/login' — 啟動 dev server 後自動開啟瀏覽器並導向登入頁
     * 方便初學者不用手動輸入網址，類似 Grails 的 run-app 會提示 URL
     */
    open: '/login',

    /**
     * proxy — 開發時 API 代理轉發
     *
     * 【為什麼需要 proxy？】
     * 瀏覽器有「同源政策」：前端在 localhost:5173，API 在 localhost:8083，
     * 直接 fetch 會被 CORS 擋下。proxy 讓前端只請求同源的 /api，由 Vite 轉發到後端。
     *
     * 範例：axios.get('/api/v1/orders')
     *   → 瀏覽器看到：http://localhost:5173/api/v1/orders
     *   → Vite 轉發到：http://localhost:8083/api/v1/orders
     */
    proxy: {
      '/api': {
        target: 'http://localhost:8083', // 後端 Spring Boot 位址
        changeOrigin: true               // 修改請求頭 Host，讓後端正確識別來源
      }
    }
  },

  /**
   * build — 打包（npm run build）專用設定
   */
  build: {
    /**
     * outDir — 打包輸出目錄（相對於 frontend/ 資料夾）
     * 輸出到 ../server/public，讓 Node BFF 可直接服務這些靜態檔
     */
    outDir: '../server/public',

    /**
     * emptyOutDir — 打包前清空輸出目錄
     * 避免舊版 JS 殘留造成快取或載入錯誤
     */
    emptyOutDir: true
  }
});
