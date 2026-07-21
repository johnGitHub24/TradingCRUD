<!--
  =============================================================================
  OrdersView.vue — 訂單管理主頁（CRUD + 批次操作）
  =============================================================================

  【這個檔案是什麼？】
  對應路由 /orders（需登入），展示訂單列表、篩選、分頁，ADMIN 可新增/編輯/刪除。

  【頁面架構示意】
  ┌─────────────────────────────────────────────┐
  │  工具列（ADMIN：批次新增、新增訂單）         │
  ├─────────────────────────────────────────────┤
  │  篩選區（symbol / status）→ 點「查詢」觸發   │
  ├─────────────────────────────────────────────┤
  │  表格（v-for 渲染 orders）                   │
  │  - 一般使用者：唯讀                           │
  │  - ADMIN：勾選、編輯、刪除、批次刪除          │
  ├─────────────────────────────────────────────┤
  │  分頁 footer + Modal（新增/編輯共用）         │
  └─────────────────────────────────────────────┘

  【學習重點 — 本頁使用的 Vue 概念】
  - onMounted：元件掛載後自動 loadOrders() 載入第一頁
  - ref：orders、filters、form 等響應式狀態
  - computed：hasNext、allSelected 等衍生資料
  - v-model：篩選欄位、表單、checkbox 勾選陣列
  - v-for / :key：列表渲染與高效 DOM 更新
  - v-if / v-else-if：載入中、空資料、正常列表三態
  - inject：從 App.vue 取得 showToast 全域通知
-->
<template>
  <div>
    <!--
      【v-if="auth.isAdmin"】
      依 JWT 中的 role 決定是否顯示管理功能；一般 USER 只能看列表。
      auth.isAdmin 是 computed，role 變動時按鈕會自動顯示/隱藏。
    -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="mb-0"><i class="bi bi-list-ul me-2"></i>訂單管理</h2>
      <div class="d-flex gap-2" v-if="auth.isAdmin">
        <button class="btn btn-outline-primary btn-sm" @click="openBatchCreate">
          <i class="bi bi-collection"></i> 批次新增
        </button>
        <button class="btn btn-primary btn-sm" @click="openCreate">
          <i class="bi bi-plus-circle"></i> 新增訂單
        </button>
      </div>
    </div>

    <!--
      【篩選區 — v-model 綁定 filters 物件】
      注意：輸入時只更新 filters，不會自動查詢；需點「查詢」才呼叫 loadOrders()。
      這是「手動搜尋」模式，可減少 API 請求次數；若要做即時搜尋可改用 @input + debounce。
    -->
    <div class="card mb-3">
      <div class="card-body py-2">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small mb-1">標的</label>
            <input v-model="filters.symbol" class="form-control form-control-sm" placeholder="BTCUSDT" />
          </div>
          <div class="col-md-3">
            <label class="form-label small mb-1">狀態</label>
            <select v-model="filters.status" class="form-select form-select-sm">
              <option value="">全部</option>
              <!-- v-for 遍歷 statuses 陣列產生下拉選項；:key 幫助 Vue 追蹤節點 -->
              <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-sm btn-secondary w-100" @click="loadOrders">
              <i class="bi bi-search"></i> 查詢
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 【資料表格 — 三種顯示狀態】loading / 空資料 / 正常列表 -->
    <div class="card shadow-sm">
      <div class="table-responsive">
        <table class="table table-hover table-striped mb-0">
          <thead class="table-dark">
            <tr>
              <!-- ADMIN 專用全選 checkbox -->
              <th v-if="auth.isAdmin" style="width:40px">
                <!-- :checked 綁定 computed allSelected；@change 觸發 toggleSelectAll -->
                <input type="checkbox" :checked="allSelected" @change="toggleSelectAll" />
              </th>
              <th>ID</th>
              <th>客戶單號</th>
              <th>標的</th>
              <th>方向</th>
              <th>數量</th>
              <th>價格</th>
              <th>狀態</th>
              <th>建立時間</th>
              <th v-if="auth.isAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 狀態 1：載入中 -->
            <tr v-if="loading">
              <!-- :colspan 動態計算欄位數（ADMIN 多兩欄） -->
              <td :colspan="auth.isAdmin ? 10 : 8" class="text-center py-4">
                <span class="spinner-border spinner-border-sm"></span> 載入中...
              </td>
            </tr>
            <!-- 狀態 2：無資料（v-else-if 與上方 v-if 互斥） -->
            <tr v-else-if="orders.length === 0">
              <td :colspan="auth.isAdmin ? 10 : 8" class="text-center text-muted py-4">尚無訂單資料</td>
            </tr>
            <!-- 狀態 3：正常列表 — v-for 遍歷 orders，:key="order.id" 必填 -->
            <tr v-for="order in orders" :key="order.id">
              <td v-if="auth.isAdmin">
                <!--
                  【v-model 綁定陣列 selectedIds】
                  checkbox 的 :value 為 order.id；勾選時 id 自動 push 進 selectedIds，取消則移除。
                  這是 Vue 處理「多選 checkbox」的標準寫法。
                -->
                <input type="checkbox" :value="order.id" v-model="selectedIds" />
              </td>
              <td>{{ order.id }}</td>
              <td><code>{{ order.clientOrderId }}</code></td>
              <td>{{ order.symbol }}</td>
              <td>
                <!-- :class 動態樣式：BUY 綠色、SELL 紅色 badge -->
                <span :class="order.side === 'BUY' ? 'badge bg-success' : 'badge bg-danger'">
                  {{ order.side }}
                </span>
              </td>
              <td>{{ order.quantity }}</td>
              <td>{{ formatPrice(order.price) }}</td>
              <td><span class="badge bg-secondary">{{ order.status }}</span></td>
              <td class="small">{{ formatDate(order.createdAt) }}</td>
              <td v-if="auth.isAdmin">
                <button class="btn btn-sm btn-outline-primary me-1" @click="openEdit(order)">
                  <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="handleDelete(order.id)">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分頁 footer：meta 來自後端 PagedResponse -->
      <div class="card-footer d-flex justify-content-between align-items-center">
        <span class="small text-muted">共 {{ meta.total }} 筆</span>
        <div class="d-flex gap-2 align-items-center">
          <button v-if="auth.isAdmin && selectedIds.length" class="btn btn-sm btn-outline-danger" @click="handleBatchDelete">
            批次刪除 ({{ selectedIds.length }})
          </button>
          <!-- :disabled 綁定條件，防止翻到不存在的頁 -->
          <button class="btn btn-sm btn-outline-secondary" :disabled="meta.page <= 0" @click="prevPage">上一頁</button>
          <span class="small">第 {{ meta.page + 1 }} 頁</span>
          <button class="btn btn-sm btn-outline-secondary" :disabled="!hasNext" @click="nextPage">下一頁</button>
        </div>
      </div>
    </div>

    <!--
      【Modal — 新增/編輯共用】
      未使用 Bootstrap JS API，改用手動 :class + :style 控制顯示（教學簡化）。
      showModal 為 true 時加上 .show 與 display:block；editingId 區分新增/編輯模式。
    -->
    <div class="modal fade" :class="{ show: showModal }" :style="{ display: showModal ? 'block' : 'none' }" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editingId ? '編輯訂單' : '新增訂單' }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <form @submit.prevent="handleSubmit">
            <div class="modal-body">
              <!-- 客戶單號僅新增時可填；編輯時後端通常不允許改 clientOrderId -->
              <div v-if="!editingId" class="mb-3">
                <label class="form-label">客戶單號 *</label>
                <input v-model="form.clientOrderId" class="form-control" required />
              </div>
              <div class="mb-3">
                <label class="form-label">標的 *</label>
                <input v-model="form.symbol" class="form-control" required />
              </div>
              <div class="row g-2">
                <div class="col-6">
                  <label class="form-label">方向 *</label>
                  <select v-model="form.side" class="form-select" required>
                    <option value="BUY">BUY</option>
                    <option value="SELL">SELL</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label">狀態</label>
                  <select v-model="form.status" class="form-select">
                    <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
                  </select>
                </div>
              </div>
              <div class="row g-2 mt-1">
                <div class="col-6">
                  <label class="form-label">數量 *</label>
                  <!-- v-model.number：自動把輸入轉成數字型別，避免字串 "0.5" 送 API -->
                  <input v-model.number="form.quantity" type="number" step="0.0001" min="0.0001" class="form-control" required />
                </div>
                <div class="col-6">
                  <label class="form-label">價格 *</label>
                  <input v-model.number="form.price" type="number" step="0.01" min="0.01" class="form-control" required />
                </div>
              </div>
              <div v-if="formError" class="alert alert-danger mt-3 py-2">{{ formError }}</div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="submitting">儲存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <!-- Modal 半透明背景遮罩 -->
    <div v-if="showModal" class="modal-backdrop fade show"></div>
  </div>
</template>

<script setup>
/**
 * =============================================================================
 * OrdersView — Script 邏輯層
 * =============================================================================
 *
 * 【狀態變數一覽】
 * | 變數          | 型別   | 用途                              |
 * |---------------|--------|-----------------------------------|
 * | orders        | ref[]  | 當前頁訂單列表（API 回傳 data）    |
 * | meta          | ref    | 分頁 { page, size, total }        |
 * | filters       | ref    | 篩選 { symbol, status }           |
 * | selectedIds   | ref[]  | 勾選的 id（批次刪除）              |
 * | showModal     | ref    | Modal 顯示開關                    |
 * | editingId     | ref    | null=新增；數字=編輯該筆          |
 * | form          | ref    | Modal 表單物件（v-model 綁定）    |
 *
 * 【學習重點 — onMounted】
 * 生命週期鉤子：DOM 掛載完成後執行一次 loadOrders()。
 * 類似 jQuery 的 $(document).ready()，但綁定在元件生命週期上。
 *
 * 【學習重點 — computed】
 * hasNext、allSelected 依賴 meta/orders/selectedIds 自動重算，不必手動更新。
 *
 * 【學習重點 — inject】
 * 從 App.vue 的 provide('showToast') 取得函式，無需 props 一層層傳遞。
 */
import { ref, computed, onMounted, inject } from 'vue';
import { useAuthStore } from '../stores/auth';
import {
  fetchOrders, createOrder, updateOrder, deleteOrder,
  batchCreateOrders, batchDeleteOrders
} from '../api/client';

const auth = useAuthStore();

/** inject 第二個參數可設預設值；此處假設 App.vue 一定會 provide */
const showToast = inject('showToast');

const orders = ref([]);
const loading = ref(false);
const meta = ref({ page: 0, size: 20, total: 0 });
const filters = ref({ symbol: '', status: '' });
const selectedIds = ref([]);
const showModal = ref(false);
const editingId = ref(null);
const submitting = ref(false);
const formError = ref('');

/** 與後端 OrderStatus enum 一致，供下拉選單使用 */
const statuses = ['NEW', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED'];

/** 產生空白表單預設值；clientOrderId 用時間戳降低重複機率 */
const emptyForm = () => ({
  clientOrderId: `o-${Date.now()}`,
  symbol: 'BTCUSDT',
  side: 'BUY',
  quantity: 0.5,
  price: 65000,
  status: 'NEW'
});
const form = ref(emptyForm());

/**
 * 【computed — 是否還有下一頁】
 * 當 meta 或 orders 變動時自動重算；template 中 :disabled="!hasNext" 會響應更新。
 */
const hasNext = computed(() => (meta.value.page + 1) * meta.value.size < meta.value.total);

/**
 * 【computed — 表頭全選 checkbox 的 checked 狀態】
 * 當前頁全部勾選時為 true，供 :checked="allSelected" 使用。
 */
const allSelected = computed(() => orders.value.length > 0 && selectedIds.value.length === orders.value.length);

/** 格式化工具函式 — 純函式，非響應式，放在 setup 頂層即可 */
function formatPrice(v) { return Number(v).toLocaleString('zh-TW'); }
function formatDate(v) { return v ? new Date(v).toLocaleString('zh-TW') : '-'; }

/**
 * 載入訂單列表
 * 組裝 query params → GET /api/v1/orders → 更新 orders 與 meta
 * 每次查詢會清空 selectedIds，避免跨頁勾選錯亂
 */
async function loadOrders() {
  loading.value = true;
  try {
    const params = { page: meta.value.page, size: meta.value.size };
    if (filters.value.symbol) params.symbol = filters.value.symbol;
    if (filters.value.status) params.status = filters.value.status;
    const data = await fetchOrders(params);
    orders.value = data.data;
    meta.value = { ...meta.value, ...data.meta };
    selectedIds.value = [];
  } catch (e) {
    showToast(e.response?.data?.detail || '載入失敗', 'danger');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  form.value = emptyForm();
  formError.value = '';
  showModal.value = true;
}

/** 編輯：淺拷貝 order 到 form（{ ...order }） */
function openEdit(order) {
  editingId.value = order.id;
  form.value = { ...order };
  formError.value = '';
  showModal.value = true;
}

function closeModal() { showModal.value = false; }

/**
 * 表單送出 — 依 editingId 分流 create / update
 * 成功後關閉 Modal 並 reload 列表，保持畫面與後端同步
 */
async function handleSubmit() {
  submitting.value = true;
  formError.value = '';
  try {
    if (editingId.value) {
      await updateOrder(editingId.value, {
        symbol: form.value.symbol,
        side: form.value.side,
        quantity: form.value.quantity,
        price: form.value.price,
        status: form.value.status
      });
      showToast('訂單已更新', 'success');
    } else {
      await createOrder({
        clientOrderId: form.value.clientOrderId,
        symbol: form.value.symbol,
        side: form.value.side,
        quantity: form.value.quantity,
        price: form.value.price
      });
      showToast('訂單已建立', 'success');
    }
    closeModal();
    await loadOrders();
  } catch (e) {
    formError.value = e.response?.data?.detail || e.response?.data?.message || '操作失敗';
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id) {
  if (!confirm('確定要刪除此訂單？')) return;
  try {
    await deleteOrder(id);
    showToast('已刪除', 'warning');
    await loadOrders();
  } catch (e) {
    showToast(e.response?.data?.detail || '刪除失敗', 'danger');
  }
}

async function handleBatchDelete() {
  if (!confirm(`確定要刪除 ${selectedIds.value.length} 筆訂單？`)) return;
  try {
    const result = await batchDeleteOrders(selectedIds.value);
    showToast(`批次刪除：成功 ${result.succeeded}，失敗 ${result.failed}`, result.failed ? 'warning' : 'success');
    await loadOrders();
  } catch (e) {
    showToast('批次刪除失敗', 'danger');
  }
}

/**
 * 批次新增示範 — 實務可改為 Modal 讓使用者輸入多筆 JSON/CSV
 * 一次 POST 兩筆到 /api/v1/orders/batch
 */
async function openBatchCreate() {
  const orders = [
    { clientOrderId: `b-${Date.now()}-1`, symbol: 'ETHUSDT', side: 'BUY', quantity: 1, price: 3200 },
    { clientOrderId: `b-${Date.now()}-2`, symbol: 'BTCUSDT', side: 'SELL', quantity: 0.1, price: 66000 }
  ];
  try {
    const result = await batchCreateOrders(orders);
    showToast(`批次新增：成功 ${result.succeeded}，失敗 ${result.failed}`, result.failed ? 'warning' : 'success');
    await loadOrders();
  } catch (e) {
    showToast('批次新增失敗', 'danger');
  }
}

/** 表頭全選 / 取消全選 */
function toggleSelectAll(e) {
  selectedIds.value = e.target.checked ? orders.value.map((o) => o.id) : [];
}

function prevPage() { meta.value.page--; loadOrders(); }
function nextPage() { meta.value.page++; loadOrders(); }

/**
 * 【onMounted — 元件生命週期】
 * 第一次進入 /orders 時自動載入資料，無需在 template 手動觸發。
 * 若從其他頁返回，預設不會重跑 onMounted（可用 onActivated 配合 keep-alive）。
 */
onMounted(loadOrders);
</script>
