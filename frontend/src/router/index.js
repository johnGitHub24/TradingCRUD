/**
 * =============================================================================
 * router/index.js — Vue Router 路由設定與導航守衛（Navigation Guard）
 * =============================================================================
 *
 * 【這個檔案是什麼？】
 * 定義「網址 ↔ 頁面元件」的對應關係，並在每次切換路由前檢查登入狀態。
 * 類比後端：像 Spring MVC 的 @RequestMapping，或 Grails 的 UrlMappings.groovy。
 *
 * 【學習重點 — Vue Router 核心概念】
 *
 * 1. createWebHistory()
 *    使用 HTML5 History API，網址為 /orders（無 # 號）。
 *    另可選 createWebHashHistory() 產生 /#/orders，較不需伺服器額外設定。
 *
 * 2. routes 陣列
 *    每筆 route 定義：path（網址）、component（要渲染的 .vue）、meta（自訂中繼資料）。
 *
 * 3. meta.requiresAuth / meta.guest
 *    自訂旗標，供 beforeEach 守衛讀取，實現「需登入」與「僅訪客」頁面分流。
 *
 * 4. beforeEach — 全域前置守衛（Router Guard）
 *    每次導航「之前」執行。可 return 新路徑字串以重定向，或 return false 取消導航。
 *
 * 【重要：前端防護 ≠ 後端授權】
 * 路由守衛只是 UX 層：避免未登入使用者看到訂單頁空白或錯誤。
 * 真正的 JWT 驗證仍由 Spring Security 在後端執行；繞過前端仍無法呼叫 API。
 *
 * 【學習重點 — 為什麼要用 unref(auth.isLoggedIn)？】
 * isLoggedIn 是 computed ref，在 <template> 中 Vue 會自動解包（unwrap），
 * 但在普通 JavaScript（如 beforeEach 回呼）中，它仍是 Ref 物件。
 * 若寫 if (!auth.isLoggedIn) 會永遠為 false（物件為 truthy），守衛失效。
 * 必須用 unref() 或 auth.isLoggedIn.value 取得實際布林值。
 * 詳見 stores/auth.js 的說明。
 */
import { createRouter, createWebHistory } from 'vue-router';
import { unref } from 'vue';
import { useAuthStore } from '../stores/auth';
import LoginView from '../views/LoginView.vue';
import OrdersView from '../views/OrdersView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    /**
     * redirect — 造訪根路徑 / 時自動導向 /orders
     * 使用者輸入 http://localhost:5173/ 會被導到訂單頁（再由守衛決定是否需登入）
     */
    { path: '/', redirect: '/orders' },

    /**
     * 登入頁
     * meta.guest: true — 標記為「訪客專用」；已登入使用者不應停留在此頁
     */
    { path: '/login', name: 'login', component: LoginView, meta: { guest: true } },

    /**
     * 訂單 CRUD 主頁
     * meta.requiresAuth: true — 必須有有效 JWT 才能進入
     */
    { path: '/orders', name: 'orders', component: OrdersView, meta: { requiresAuth: true } }
  ]
});

/**
 * 【全域前置守衛 beforeEach】
 *
 * 參數 to：即將進入的路由物件（含 path、meta、params 等）
 * 參數 from：離開的路由（本專案未使用，可省略）
 *
 * 回傳值：
 * - 不 return 或 return true → 放行
 * - return '/login' → 取消原導航，改導向登入頁
 * - return false → 取消導航
 */
router.beforeEach((to) => {
  const auth = useAuthStore();

  /**
   * 【unref — 從 ref/computed 取出原始值】
   * auth.isLoggedIn 型別為 ComputedRef<boolean>
   * unref() 等同於：若為 ref 則取 .value，否則原樣回傳
   */
  const loggedIn = unref(auth.isLoggedIn);

  // 目標頁需要登入，但使用者未登入 → 導向登入頁
  if (to.meta.requiresAuth && !loggedIn) {
    return '/login';
  }

  // 目標頁為訪客頁（登入頁），但使用者已登入 → 導向訂單頁，避免重複登入
  if (to.meta.guest && loggedIn) {
    return '/orders';
  }
});

export default router;
