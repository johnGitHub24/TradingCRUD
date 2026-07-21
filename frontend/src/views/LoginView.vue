<!--
  =============================================================================
  LoginView.vue — 登入頁面元件
  =============================================================================

  【這個檔案是什麼？】
  對應路由 /login，提供帳號密碼表單，驗證成功後寫入 JWT 並導向訂單頁。

  【運作流程】
  1. 使用者填寫帳密 → 點擊「登入」（@submit.prevent 阻止表單預設整頁提交）
  2. handleLogin() 呼叫 api/client.login() → POST /api/v1/auth/login
  3. 成功：auth.setSession() 寫入 token → router.push('/orders')
  4. 失敗：顯示後端錯誤訊息（401 帳密錯誤等）；此 401 不會觸發 client.js 的全域登出

  【學習重點 — 本頁使用的 Vue 概念】
  - v-model：雙向資料綁定，輸入框與 ref 變數同步
  - ref：響應式變數（username、password、loading、error）
  - v-if：條件顯示錯誤訊息區塊
  - :disabled：動態屬性綁定，loading 時禁用按鈕防止重複提交
  - @submit.prevent：事件修飾符，阻止預設行為並呼叫 handleLogin
  - async/await：非同步呼叫 API，try/catch/finally 處理成功與錯誤
-->
<template>
  <div class="row justify-content-center">
    <div class="col-md-5 col-lg-4">
      <div class="card shadow-sm">
        <div class="card-header bg-primary text-white text-center py-3">
          <h4 class="mb-0"><i class="bi bi-shield-lock me-2"></i>TradingCRUD 登入</h4>
        </div>
        <div class="card-body p-4">
          <!--
            @submit.prevent 說明：
            - @submit 監聽表單提交（Enter 鍵或點擊 submit 按鈕）
            - .prevent 等同 event.preventDefault()，避免瀏覽器整頁 reload
            - 改由 Vue 的 handleLogin 以 AJAX 方式呼叫後端
          -->
          <form @submit.prevent="handleLogin">
            <div class="mb-3">
              <label class="form-label">帳號</label>
              <!--
                【v-model — 雙向綁定】
                輸入框 value ↔ script 中的 username ref 同步。
                使用者打字 → username.value 更新；程式改 username.value → 輸入框跟著變。
                required：HTML5 原生驗證，空白時無法提交。
              -->
              <input v-model="username" type="text" class="form-control" required autocomplete="username" />
            </div>
            <div class="mb-3">
              <label class="form-label">密碼</label>
              <input v-model="password" type="password" class="form-control" required autocomplete="current-password" />
            </div>

            <!--
              【v-if — 條件渲染】
              僅當 error 有內容時才渲染此 div，避免空白紅框佔位。
              {{ error }} 顯示 ref 字串內容。
            -->
            <div v-if="error" class="alert alert-danger py-2">{{ error }}</div>

            <!--
              :disabled="loading" — 請求進行中禁用按鈕，防止連點造成重複登入請求
              v-if="loading" — 顯示 Bootstrap spinner 作為載入中視覺回饋
            -->
            <button type="submit" class="btn btn-primary w-100" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              登入
            </button>
          </form>

          <!-- 開發用提示：預設測試帳號 -->
          <p class="text-muted small mt-3 mb-0 text-center">
            預設帳號：admin / admin123
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * =============================================================================
 * LoginView — Script 邏輯層
 * =============================================================================
 *
 * 【學習重點 — ref()】
 * ref() 把基本型別（字串、布林）包成響應式物件。
 * - 在 template 中：直接寫 username（自動解包）
 * - 在 script 中：必須寫 username.value 讀寫
 *
 * 【學習重點 — useRouter()】
 * 取得 Vue Router 實例，login 成功後用 router.push('/orders') 程式化導航。
 * 不會整頁 reload，只替換 <router-view> 內的元件。
 *
 * 【學習重點 — useAuthStore()】
 * 登入成功後呼叫 setSession，將 JWT 寫入 reactive state + localStorage，
 * 之後 axios 攔截器與路由守衛都能讀到登入狀態。
 */
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api/client';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();

/** 表單欄位 — 預填 admin 方便本地開發測試，正式環境可改為空字串 */
const username = ref('admin');
const password = ref('admin123');

/** loading：控制按鈕 disabled 與 spinner 顯示 */
const loading = ref(false);

/** error：登入失敗時顯示的錯誤訊息（由 catch 區塊賦值） */
const error = ref('');

/**
 * 登入主流程（async 函式）
 *
 * Step 1: loading = true，清空上次錯誤
 * Step 2: 呼叫 login() → axios POST /api/v1/auth/login
 * Step 3: 成功 → setSession 儲存 JWT → router.push 導向訂單頁
 * Step 4: 失敗 → 從 error.response.data 取出後端錯誤訊息顯示
 * Step 5: finally 無論成敗都 loading = false
 *
 * 【與 client.js 401 攔截的關係】
 * 登入失敗回 401 時，攔截器因 isLoginRequest 為 true 而不會整頁重導，
 * 錯誤會傳到這裡的 catch，由 error.value 顯示給使用者。
 */
async function handleLogin() {
  loading.value = true;
  error.value = '';
  try {
    const data = await login(username.value, password.value);
    auth.setSession({
      accessToken: data.accessToken,
      username: data.username,
      role: data.role
    });
    router.push('/orders');
  } catch (e) {
    // 後端 Spring ProblemDetail 格式：detail 或 message 欄位
    error.value = e.response?.data?.detail || e.response?.data?.message || '登入失敗，請確認帳密';
  } finally {
    loading.value = false;
  }
}
</script>
