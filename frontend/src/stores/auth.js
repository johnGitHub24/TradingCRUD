/**
 * =============================================================================
 * stores/auth.js — 認證狀態管理（輕量 Store，不依賴 Pinia）
 * =============================================================================
 *
 * 【這個檔案是什麼？】
 * 集中管理「登入狀態」：JWT token、使用者名稱、角色，並同步到 localStorage。
 * 全專案透過 useAuthStore() 讀寫同一份 state，類似 Pinia/Vuex 的簡化版。
 *
 * 【為什麼不用 Pinia？】
 * 本教學專案狀態簡單，用 Vue 內建的 reactive + computed 即可示範響應式原理，
 * 減少學習負擔。正式大型專案建議改用 Pinia（官方推薦的狀態管理庫）。
 *
 * 【學習重點 — reactive vs ref】
 * - reactive({})：把「物件」變成響應式，直接改 state.token 會觸發更新
 * - ref()：常用於單一值或陣列；在 script 中需 .value 存取
 * 本檔用 reactive 包一整包認證欄位，語意較清晰。
 *
 * 【學習重點 — computed】
 * isLoggedIn = computed(() => !!state.token)
 * 當 state.token 變動時，isLoggedIn 自動重新計算。
 * 在 <template> 中寫 auth.isLoggedIn 時，Vue 會自動解包，顯示 true/false。
 *
 * =============================================================================
 * 【重要：unref 問題 — 路由守衛必讀】
 * =============================================================================
 *
 * useAuthStore() 回傳的 isLoggedIn 是 ComputedRef（一種 ref），不是純布林值。
 *
 *   ❌ 錯誤寫法（在 router/index.js 的 beforeEach 中）：
 *      if (!auth.isLoggedIn) { ... }
 *      → auth.isLoggedIn 是物件，永遠 truthy，!auth.isLoggedIn 永遠 false
 *      → 未登入使用者也能進入 /orders！
 *
 *   ✅ 正確寫法：
 *      import { unref } from 'vue';
 *      const loggedIn = unref(auth.isLoggedIn);
 *      或：auth.isLoggedIn.value
 *
 * 【為什麼 template 裡不用 unref？】
 * Vue 編譯 template 時會自動對 ref/computed 做「解包」（auto-unwrap），
 * 所以 v-if="auth.isLoggedIn" 在 .vue 模板中是正確的。
 * 但在 .js 檔的普通 JavaScript 邏輯裡，沒有這層魔法，必須手動取 .value 或 unref。
 *
 * 【token 為何用 getter，不用 computed？】
 * axios 攔截器需要讀取「當下」的字串 token 加到 Authorization header，
 * 用 get token() { return state.token } 每次存取都拿到最新值，語意直觀。
 */
import { reactive, computed } from 'vue';

/** localStorage 鍵名常數 — 集中定義避免拼字錯誤，也方便日後改名 */
const TOKEN_KEY = 'trading_crud_token';
const USER_KEY = 'trading_crud_user';
const ROLE_KEY = 'trading_crud_role';

/**
 * 【reactive — 模組級單例狀態】
 * 整個應用共用這一份 state；重新整理頁面時從 localStorage 還原，保持登入狀態。
 */
const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  username: localStorage.getItem(USER_KEY) || '',
  role: localStorage.getItem(ROLE_KEY) || ''
});

/**
 * 登入成功後寫入 session
 * 同時更新 memory（state）與持久化（localStorage），重新整理頁面仍保持登入。
 *
 * @param {{ accessToken: string, username: string, role: string }} session
 */
function setSession({ accessToken, username, role }) {
  state.token = accessToken;
  state.username = username;
  state.role = role;
  localStorage.setItem(TOKEN_KEY, accessToken);
  localStorage.setItem(USER_KEY, username);
  localStorage.setItem(ROLE_KEY, role);
}

/** 登出：清空 state 並移除 localStorage，isLoggedIn 會自動變為 false */
function logout() {
  state.token = '';
  state.username = '';
  state.role = '';
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(ROLE_KEY);
}

/**
 * 【computed — 衍生狀態】
 * 依賴 state.token，有 token 即視為已登入。
 * 回傳值為 ComputedRef<boolean>，模板自動解包，JS 邏輯需 unref 或 .value。
 */
const isLoggedIn = computed(() => !!state.token);

/** 是否為管理員 — 用於 OrdersView 顯示/隱藏 CRUD 按鈕 */
const isAdmin = computed(() => state.role === 'ADMIN');

/**
 * 取得認證 Store（Composable 風格）
 *
 * 每次呼叫 useAuthStore() 都回傳同一組 state 的介面，不是建立新實例。
 * 命名慣例 useXxx 來自 Composition API，表示「在 setup 中使用的邏輯函式」。
 */
export function useAuthStore() {
  return {
    /** getter：讀取當前 JWT 字串，供 axios request interceptor 使用 */
    get token() { return state.token; },
    get username() { return state.username; },
    get role() { return state.role; },
    isLoggedIn,
    isAdmin,
    setSession,
    logout
  };
}
