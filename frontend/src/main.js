/**
 * =============================================================================
 * main.js — Vue 3 應用程式進入點（Entry Point）
 * =============================================================================
 *
 * 【這個檔案是什麼？】
 * 整個前端應用的「啟動程式」。index.html 載入此檔後，Vue 才開始運作。
 * 類比 Spring Boot 的 @SpringBootApplication 主類別，或 Grails 的 Application.groovy。
 *
 * 【學習重點 — createApp 三步驟】
 *
 *   createApp(App)     → 建立 Vue 應用實例，指定根元件為 App.vue
 *        .use(router)  → 安裝 Vue Router 外掛，全專案可使用 useRouter()、<router-view>
 *        .mount('#app')→ 把 App.vue 渲染到 index.html 的 <div id="app"> 節點
 *
 * 【開發 vs 正式環境】
 * - 開發：npm run dev → Vite :5173，API 由 vite.config.js proxy 轉發至 Spring Boot :8083
 * - 正式：npm run build → 靜態檔輸出至 server/public，由 Node BFF 統一服務前端與 API 代理
 *
 * 【為什麼根元件是 App.vue？】
 * App.vue 負責全站共用版面（導覽列、Toast、<router-view> 插槽），
 * 各「頁面」由 Router 動態載入 LoginView / OrdersView，符合元件化設計。
 */
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// 鏈式呼叫：建立 → 註冊路由 → 掛載 DOM（順序不可顛倒，mount 必須最後）
createApp(App).use(router).mount('#app');
