<!--
  =============================================================================
  App.vue — 根元件（Root Component）
  =============================================================================

  【這個檔案是什麼？】
  Vue 應用的最上層元件，所有其他元件都是它的子孫。
  負責：全站導覽列、頁面內容區、全域 Toast 通知。

  【學習重點 — .vue 單檔元件結構】
  一個 .vue 檔通常包含三塊（本專案用 <script setup> 省略 style）：
    <template>  → HTML 模板（含 Vue 指令如 v-if、v-for、@click）
    <script setup> → JavaScript 邏輯（Composition API）
    <style>     → 元件專用 CSS（可選）

  【本檔案使用的 Vue 概念】
  - v-if：條件渲染，auth.isLoggedIn 為 true 才顯示導覽列
  - <router-view>：Vue Router 的「出口」，依網址渲染對應的 View 元件
  - v-for：迴圈渲染 Toast 列表
  - provide/inject：跨層級傳遞 showToast 函式，避免 props 一層層往下傳（props drilling）
  - ref：建立響應式陣列 toasts，資料變動時模板自動更新
-->
<template>
  <div class="min-vh-100 bg-light">
    <!--
      【條件渲染 v-if】
      僅在已登入時顯示頂部導覽列；登入頁（/login）不會看到這段 HTML。
      auth.isLoggedIn 是 computed 屬性，token 變動時會自動重新計算並更新畫面。
    -->
    <nav v-if="auth.isLoggedIn" class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container-fluid">
        <span class="navbar-brand">
          <i class="bi bi-graph-up-arrow me-2"></i>TradingCRUD
        </span>
        <div class="d-flex align-items-center gap-3 text-white">
          <!-- 雙大括號 {{ }} 是文字插值，顯示 auth store 中的 username、role -->
          <span class="small">
            <i class="bi bi-person-circle me-1"></i>{{ auth.username }}
            <span class="badge bg-light text-primary ms-1">{{ auth.role }}</span>
          </span>
          <!-- @click 是 v-on:click 的簡寫，點擊時執行 logout 函式 -->
          <button class="btn btn-outline-light btn-sm" @click="logout">
            <i class="bi bi-box-arrow-right"></i> 登出
          </button>
        </div>
      </div>
    </nav>

    <!--
      【router-view — 頁面切換的核心】
      Vue Router 根據當前網址，把對應元件（LoginView 或 OrdersView）渲染在這裡。
      切換路由時只有這塊 DOM 會替換，外層 nav 與 Toast 區塊保持不變。
    -->
    <main class="container py-4">
      <router-view />
    </main>

    <!--
      【全域 Toast 通知區】
      v-for 遍歷 toasts 陣列，:key 用唯一 id 幫助 Vue 高效更新 DOM（diff 演算法）。
      :class 動態綁定 Bootstrap 色彩類別（text-bg-success、danger 等）。
    -->
    <div class="toast-container position-fixed bottom-0 end-0 p-3">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast show"
        :class="`text-bg-${toast.type}`"
        role="alert"
      >
        <div class="toast-body">{{ toast.message }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * =============================================================================
 * App.vue — Script 邏輯層（Composition API + script setup）
 * =============================================================================
 *
 * 【什麼是 <script setup>？】
 * Vue 3 推薦的語法糖：頂層宣告的變數、函式會自動暴露給 template 使用，
 * 無需像 Options API 那樣寫在 export default { data, methods } 裡。
 *
 * 【本檔案使用的 Vue API】
 * - ref()：包裝基本型別或物件，變成響應式（.value 在 script 中讀寫，template 中可省略 .value）
 * - provide()：向所有子元件提供資料或函式（對應子元件的 inject）
 * - useRouter()：取得路由實例，可程式化導航（router.push）
 * - useAuthStore()：本專案自製的認證狀態模組（非 Pinia，輕量 reactive + computed）
 */
import { ref, provide } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
const router = useRouter();

/**
 * 【ref — 響應式變數】
 * toasts 是 Toast 訊息佇列，每則有 { id, message, type }。
 * 用 ref([]) 包裝後，push / filter 會觸發畫面重新渲染。
 */
const toasts = ref([]);
let toastId = 0; // 非響應式計數器即可，僅用於產生唯一 id

/**
 * 顯示右下角 Toast 通知
 *
 * @param {string} message - 顯示文字
 * @param {string} type - Bootstrap 色彩：info | success | warning | danger
 *
 * 【為什麼用 provide 而不是全域變數？】
 * provide 是 Vue 官方推薦的「依賴注入」方式，子元件 inject('showToast') 即可使用，
 * 比 window.showToast 更易測試，且生命週期與元件樹綁定。
 */
function showToast(message, type = 'info') {
  const id = ++toastId;
  toasts.value.push({ id, message, type });
  // 3 秒後從佇列移除該則 Toast（filter 產生新陣列，觸發響應式更新）
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id);
  }, 3000);
}

/** 向所有子元件（含 OrdersView）提供 showToast，子元件用 inject('showToast') 取得 */
provide('showToast', showToast);

/**
 * 登出流程
 * 1. auth.logout() — 清空 reactive state 與 localStorage 中的 JWT
 * 2. router.push('/login') — 程式化導航至登入頁（不整頁 reload）
 * 3. showToast — 提示使用者已登出
 */
function logout() {
  auth.logout();
  router.push('/login');
  showToast('已登出', 'warning');
}
</script>
