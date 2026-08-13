package com.trading.crud.order;

import com.trading.crud.common.DuplicateResourceException;
import com.trading.crud.common.ResourceNotFoundException;
import com.trading.crud.order.domain.OrderSide;
import com.trading.crud.order.domain.OrderStatus;
import com.trading.crud.order.dto.BatchCreateRequest;
import com.trading.crud.order.dto.BatchDeleteRequest;
import com.trading.crud.order.dto.BatchResult;
import com.trading.crud.order.dto.CreateOrderRequest;
import com.trading.crud.order.dto.OrderResponse;
import com.trading.crud.order.dto.PagedResponse;
import com.trading.crud.order.dto.UpdateOrderRequest;
import com.trading.crud.order.infrastructure.OrderEntity;
import com.trading.crud.order.infrastructure.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 【職責】OrderService 單元測試：公開 CRUD／批次方法至少一測，與整合層同 Case ID。
 * 【技巧】Mockito：{@code @Mock} Repository、{@code @Spy} 真實 Mapper、{@code @InjectMocks} Service。
 * 【概念】不啟動 Spring／DB，專注商業規則；save stub 模擬指派 id。
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest order(String clientOrderId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setClientOrderId(clientOrderId);
        request.setSymbol("BTCUSDT");
        request.setSide(OrderSide.BUY);
        request.setQuantity(new BigDecimal("0.5"));
        request.setPrice(new BigDecimal("65000.00"));
        return request;
    }

    private OrderEntity persisted(long id, String clientOrderId) {
        OrderEntity entity = new OrderEntity();
        entity.setId(id);
        entity.setClientOrderId(clientOrderId);
        entity.setSymbol("BTCUSDT");
        entity.setSide(OrderSide.BUY);
        entity.setQuantity(new BigDecimal("0.5"));
        entity.setPrice(new BigDecimal("65000.00"));
        entity.setStatus(OrderStatus.NEW);
        return entity;
    }

    @BeforeEach
    void stubSaveAssignsId() {
        AtomicLong sequence = new AtomicLong(1);
        lenient().when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(sequence.getAndIncrement());
            }
            return entity;
        });
    }

    /**
     * CASE ORDER-001：create 合法請求回傳已指派 id 的 NEW 訂單。
     * Given: clientOrderId 不存在；When: create；Then: id 非空、status=NEW。
     */
    @Test
    void ORDER_001_create_returnsPersistedNewOrder() {
        when(orderRepository.existsByClientOrderId("crud-order-001")).thenReturn(false);

        OrderResponse response = orderService.create(order("crud-order-001"));

        assertThat(response.getId()).isNotNull();
        assertThat(response.getClientOrderId()).isEqualTo("crud-order-001");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    /**
     * CASE ORDER-002：getById 找到訂單。
     * Given: Repository 回傳 id=7；When: getById(7)；Then: 回應 id／symbol 正確。
     */
    @Test
    void ORDER_002_getById_returnsMappedResponse() {
        when(orderRepository.findById(7L)).thenReturn(Optional.of(persisted(7L, "found-7")));

        OrderResponse response = orderService.getById(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getClientOrderId()).isEqualTo("found-7");
        assertThat(response.getSymbol()).isEqualTo("BTCUSDT");
    }

    /**
     * CASE SVC-001 / ORDER-006：create 遇已存在 clientOrderId 拋 DuplicateResourceException。
     * Given: existsByClientOrderId=true；When: create；Then: 拋衝突例外。
     */
    @Test
    void SVC_001_ORDER_006_create_duplicateClientOrderId_throws() {
        when(orderRepository.existsByClientOrderId("dup-1")).thenReturn(true);

        assertThatThrownBy(() -> orderService.create(order("dup-1")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    /**
     * CASE ORDER-005：update 覆寫欄位後回傳。
     * Given: 既有訂單；When: update symbol/side/status；Then: 回應已變更。
     */
    @Test
    void ORDER_005_update_overwritesMutableFields() {
        when(orderRepository.findById(3L)).thenReturn(Optional.of(persisted(3L, "upd-3")));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setSymbol("ETHUSDT");
        request.setSide(OrderSide.SELL);
        request.setQuantity(new BigDecimal("1.0"));
        request.setPrice(new BigDecimal("3200.00"));
        request.setStatus(OrderStatus.FILLED);

        OrderResponse response = orderService.update(3L, request);

        assertThat(response.getSymbol()).isEqualTo("ETHUSDT");
        assertThat(response.getSide()).isEqualTo(OrderSide.SELL);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);
    }

    /**
     * CASE ORDER-007：delete 找不到則拋 ResourceNotFoundException。
     * Given: findById 空；When: delete(99)；Then: ORDER_NOT_FOUND 語意例外。
     */
    @Test
    void ORDER_007_delete_missingId_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * CASE ORDER-008：list 夾緊異常 page／size 後回分頁 meta。
     * Given: search 回 1 筆；When: list(page=-1, size=0)；Then: meta.page=0、size=1。
     */
    @Test
    void ORDER_008_list_clampsPageAndSize() {
        OrderEntity entity = persisted(1L, "list-1");
        when(orderRepository.search(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        PagedResponse<OrderResponse> page = orderService.list(null, null, -1, 0);

        assertThat(page.getData()).hasSize(1);
        assertThat(page.getMeta().getPage()).isEqualTo(0);
        assertThat(page.getMeta().getSize()).isEqualTo(1);
        assertThat(page.getMeta().getTotal()).isEqualTo(1);
    }

    /**
     * CASE BATCH-001：batchCreate 全合法 → 全部成功。
     * Given: 兩筆不重複且 DB 無衝突；When: batchCreate；Then: succeeded=2、failed=0。
     */
    @Test
    void BATCH_001_batchCreate_allValid_countsSuccess() {
        when(orderRepository.existsByClientOrderId(anyString())).thenReturn(false);

        BatchCreateRequest request = new BatchCreateRequest();
        request.setOrders(List.of(order("a"), order("b")));

        BatchResult result = orderService.batchCreate(request);

        assertThat(result.getRequested()).isEqualTo(2);
        assertThat(result.getSucceeded()).isEqualTo(2);
        assertThat(result.getFailed()).isEqualTo(0);
        assertThat(result.getCreatedIds()).hasSize(2);
    }

    /**
     * CASE SVC-002 / BATCH-006：batchCreate 批次內重複鍵 → 成功／失敗分開計數。
     * Given: orders=[a,a,b]、DB 無衝突；When: batchCreate；Then: succeeded=2、failed=1。
     * 【技巧驗證】HashSet 偵測批次內重複，不因一筆失敗中斷整批。
     */
    @Test
    void SVC_002_BATCH_006_batchCreate_partialDuplicate_countsSuccessAndFailureSeparately() {
        when(orderRepository.existsByClientOrderId(anyString())).thenReturn(false);

        BatchCreateRequest request = new BatchCreateRequest();
        request.setOrders(List.of(order("a"), order("a"), order("b")));

        BatchResult result = orderService.batchCreate(request);

        assertThat(result.getRequested()).isEqualTo(3);
        assertThat(result.getSucceeded()).isEqualTo(2);
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getReference()).isEqualTo("a");
    }

    /**
     * CASE BATCH-007：batchDelete 混合存在／不存在 ID。
     * Given: id=1 存在、999 不存在；When: batchDelete；Then: succeeded=1、failed=1。
     */
    @Test
    void BATCH_007_batchDelete_mixedExisting_countsSeparately() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsById(999L)).thenReturn(false);

        BatchDeleteRequest request = new BatchDeleteRequest();
        request.setIds(List.of(1L, 999L));

        BatchResult result = orderService.batchDelete(request);

        assertThat(result.getRequested()).isEqualTo(2);
        assertThat(result.getSucceeded()).isEqualTo(1);
        assertThat(result.getFailed()).isEqualTo(1);
        verify(orderRepository).deleteById(eq(1L));
    }

    /**
     * CASE BATCH-008：batchDelete 皆存在 → 全成功。
     * Given: 兩個 id 都 exists；When: batchDelete；Then: failed=0。
     */
    @Test
    void BATCH_008_batchDelete_allExisting_succeeds() {
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.existsById(2L)).thenReturn(true);

        BatchDeleteRequest request = new BatchDeleteRequest();
        request.setIds(List.of(1L, 2L));

        BatchResult result = orderService.batchDelete(request);

        assertThat(result.getSucceeded()).isEqualTo(2);
        assertThat(result.getFailed()).isEqualTo(0);
    }

    /**
     * CASE BATCH-003：空清單交給 Service 仍彙總 requested=0（HTTP 400 由 @Valid 擋在整合層）。
     * Given: orders=[]；When: batchCreate；Then: requested=0、無成功無失敗。
     */
    @Test
    void BATCH_003_batchCreate_emptyList_requestedZero() {
        BatchCreateRequest request = new BatchCreateRequest();
        request.setOrders(List.of());

        BatchResult result = orderService.batchCreate(request);

        assertThat(result.getRequested()).isZero();
        assertThat(result.getSucceeded()).isZero();
        assertThat(result.getFailed()).isZero();
    }
}
