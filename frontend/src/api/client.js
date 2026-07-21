/**
 * =============================================================================
 * api/client.js — HTTP API 客戶端（Axios 封裝層）
 * =============================================================================
 *
 * 【這個檔案是什麼？】
 * 前端與後端 Spring Boot 溝通的「唯一入口」。View 層不直接 import axios，
 * 只呼叫此檔 export 的函式（login、fetchOrders 等），好處：
 *   - URL、headers 集中管理，後端路徑變更只改一處
 *   - JWT 自動附加、401 統一處理，業務元件不需重複寫攔截邏輯
 *
 * 【學習重點 — Axios Interceptors（攔截器）】
 *
 * 1. Request Interceptor（請求攔截器）
 *    在每個 HTTP 請求「送出前」執行，本專案用來自動加上 Authorization: Bearer <JWT>
 *
 * 2. Response Interceptor（回應攔截器）
 *    在收到回應「之後」執行，可統一處理錯誤（如 401 登出、500 記錄日誌）
 *
 * 【請求路徑對照表（後端 Spring Boot :8083）】
 *   login()             → POST   /api/v1/auth/login
 *   fetchOrders()       → GET    /api/v1/orders?page&size&symbol&status
 *   createOrder()       → POST   /api/v1/orders
 *   updateOrder()       → PUT    /api/v1/orders/{id}
 *   deleteOrder()       → DELETE /api/v1/orders/{id}
 *   batchCreateOrders() → POST   /api/v1/orders/batch
 *   batchDeleteOrders() → DELETE /api/v1/orders/batch
 */
import axios from 'axios';
import { useAuthStore } from '../stores/auth';

/**
 * 【axios.create — 建立專用實例】
 * 與 axios 預設實例分離，避免污染其他可能使用 axios 的程式碼。
 * baseURL: '/api/v1' 為相對路徑：
 *   - 開發時：Vite proxy 轉發至 localhost:8083
 *   - 正式時：Node BFF 或 Nginx 轉發至後端
 */
const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' }
});

/**
 * =============================================================================
 * Request Interceptor — 自動附加 JWT Bearer Token
 * =============================================================================
 *
 * 【運作時機】每次 api.get/post/put/delete 送出前都會經過此函式。
 *
 * 【為什麼登入請求也會帶 token？】
 * 若 localStorage 仍有舊 token，登入請求可能帶上過期 JWT。
 * 後端 /auth/login 為公開路徑，通常忽略 Authorization 或照常處理，不影響登入。
 * 若需嚴格區分，可判斷 config.url 是否含 '/auth/login' 再決定是否加 header。
 */
api.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config; // 必須 return config，否則請求不會送出
});

/**
 * =============================================================================
 * Response Interceptor — 401 統一處理（排除登入請求）
 * =============================================================================
 *
 * 【為什麼要攔截 401？】
 * JWT 過期或無效時，後端回 401 Unauthorized。
 * 若不處理，使用者會看到空白列表或重複錯誤 Toast；
 * 統一登出並導向 /login 可提供一致體驗。
 *
 * 【為什麼登入失敗的 401 要排除？】
 *
 *   情境：使用者輸入錯誤密碼 → POST /auth/login → 後端回 401
 *
 *   若不做排除：
 *     攔截器執行 auth.logout() + window.location.href = '/login'
 *     → 使用者看不到「帳密錯誤」的紅色提示（LoginView 的 error ref）
 *     → 可能整頁閃爍或重複導向，UX 很差
 *
 *   正確做法：
 *     isLoginRequest = url.includes('/auth/login')
 *     僅當 status === 401 && !isLoginRequest 時才自動登出重導
 *     登入的 401 交給 LoginView 的 catch 區塊顯示 error.value
 *
 * 【為什麼用 window.location.href 而不是 router.push？】
 * 攔截器在 Vue 元件外執行，無法直接使用 useRouter()。
 * 整頁導向可確保清空所有元件狀態；若已在 /login 則不重導，避免無限迴圈。
 */
api.interceptors.response.use(
  // 成功回應：直接 pass through，不做額外處理
  (response) => response,

  // 錯誤回應：依 status 分流處理
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url || '';

    /** 判斷是否為登入 API 請求（路徑含 /auth/login） */
    const isLoginRequest = url.includes('/auth/login');

    /**
     * 401 且「不是」登入請求 → Token 失效或無權限，執行全域登出流程
     * 登入請求的 401 不在此處理，由 LoginView.handleLogin 的 catch 顯示錯誤訊息
     */
    if (status === 401 && !isLoginRequest) {
      const auth = useAuthStore();
      auth.logout();
      // 避免已在登入頁時重複導向造成閃爍
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }

    // 務必 reject，讓呼叫端的 try/catch 仍能接到錯誤（如顯示 Toast）
    return Promise.reject(error);
  }
);

// =============================================================================
// 以下為業務 API 函式 — View 層只呼叫這些，不直接操作 axios 實例
// =============================================================================

/**
 * 登入
 * 不需事先有 Token；成功回傳 { accessToken, tokenType, expiresIn, username, role }
 */
export async function login(username, password) {
  const { data } = await api.post('/auth/login', { username, password });
  return data;
}

/**
 * 分頁查詢訂單列表
 * @param {{ page?: number, size?: number, symbol?: string, status?: string }} params
 * @returns 後端 PagedResponse：{ data: Order[], meta: { page, size, total } }
 */
export async function fetchOrders(params = {}) {
  const { data } = await api.get('/orders', { params });
  return data;
}

/** 新增單筆訂單（需 ADMIN 角色） */
export async function createOrder(payload) {
  const { data } = await api.post('/orders', payload);
  return data;
}

/** 更新訂單（需 ADMIN 角色） */
export async function updateOrder(id, payload) {
  const { data } = await api.put(`/orders/${id}`, payload);
  return data;
}

/** 刪除單筆訂單（需 ADMIN 角色），後端回 204 無 body */
export async function deleteOrder(id) {
  await api.delete(`/orders/${id}`);
}

/** 批次新增；部分失敗時後端可能回 207 Multi-Status */
export async function batchCreateOrders(orders) {
  const { data } = await api.post('/orders/batch', { orders });
  return data;
}

/**
 * 批次刪除
 * 注意：DELETE 請求的 body 需放在 { data: { ids } }，這是 axios 的語法
 */
export async function batchDeleteOrders(ids) {
  const { data } = await api.delete('/orders/batch', { data: { ids } });
  return data;
}

export default api;
