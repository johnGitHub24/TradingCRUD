package com.trading.crud.order;

import com.trading.crud.common.DuplicateResourceException;
import com.trading.crud.order.domain.OrderSide;
import com.trading.crud.order.dto.BatchCreateRequest;
import com.trading.crud.order.dto.BatchResult;
import com.trading.crud.order.dto.CreateOrderRequest;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 【職責】OrderService 單元測試：重複 clientOrderId、批次內部分失敗計數。
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
     * CASE SVC-001：create 遇已存在 clientOrderId 拋 DuplicateResourceException。
     * Given: existsByClientOrderId=true；When: create；Then: 拋衝突例外。
     */
    @Test
    void SVC_001_create_duplicateClientOrderId_throws() {
        when(orderRepository.existsByClientOrderId("dup-1")).thenReturn(true);

        assertThatThrownBy(() -> orderService.create(order("dup-1")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    /**
     * CASE SVC-002：batchCreate 批次內重複鍵 → 成功／失敗分開計數。
     * Given: orders=[a,a,b]、DB 無衝突；When: batchCreate；Then: succeeded=2、failed=1。
     * 【技巧驗證】HashSet 偵測批次內重複，不因一筆失敗中斷整批。
     */
    @Test
    void SVC_002_batchCreate_partialDuplicate_countsSuccessAndFailureSeparately() {
        when(orderRepository.existsByClientOrderId(anyString())).thenReturn(false);

        BatchCreateRequest request = new BatchCreateRequest();
        request.setOrders(List.of(order("a"), order("a"), order("b")));

        BatchResult result = orderService.batchCreate(request);

        assertThat(result.getRequested()).isEqualTo(3);
        assertThat(result.getSucceeded()).isEqualTo(2);
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getFailures().get(0).getReference()).isEqualTo("a");
    }
}
