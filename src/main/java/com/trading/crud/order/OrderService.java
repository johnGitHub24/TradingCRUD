package com.trading.crud.order;

import com.trading.crud.common.DuplicateResourceException;
import com.trading.crud.common.ErrorCodes;
import com.trading.crud.common.ResourceNotFoundException;
import com.trading.crud.order.domain.OrderStatus;
import com.trading.crud.order.dto.*;
import com.trading.crud.order.infrastructure.OrderEntity;
import com.trading.crud.order.infrastructure.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 【職責】訂單商業邏輯：CRUD、分頁查詢、批次建立／刪除、clientOrderId 唯一性驗證。
 * 【技巧】類別 {@code @Transactional}；唯讀方法 {@code readOnly = true}；透過 {@link OrderMapper} 轉換。
 * 【概念】交易邊界在 Service：寫入失敗可 rollback。HTTP 狀態碼由 Controller 組裝；權限由 Security 判斷。
 * 【邊界】不負責 JWT 解析、不直接回 HTTP。
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * 建構子注入 Repository 與 Mapper，便於單元測試替換依賴。
     *
     * @param orderRepository 訂單持久化介面
     * @param orderMapper     Entity ↔ DTO 轉換器
     */
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    /**
     * 【職責】建立單筆訂單；clientOrderId 已存在則拋衝突。
     * 【技巧】先 {@code existsByClientOrderId} 再 save；衝突拋 {@link DuplicateResourceException}。
     * 【概念】應用層先檢查可給出清楚錯誤碼；DB unique 是最後防線，兩者可並存。
     *
     * @param request 建立請求
     * @return 已持久化訂單回應
     * @throws DuplicateResourceException clientOrderId 已存在時
     */
    public OrderResponse create(CreateOrderRequest request) {
        if (orderRepository.existsByClientOrderId(request.getClientOrderId())) {
            throw new DuplicateResourceException(ErrorCodes.DUPLICATE_ORDER,
                    "clientOrderId already exists: " + request.getClientOrderId());
        }
        OrderEntity saved = orderRepository.save(orderMapper.toEntity(request));
        return orderMapper.toResponse(saved);
    }

    /**
     * 【職責】依 ID 查詢訂單。
     * 【技巧】{@code @Transactional(readOnly = true)}；找不到拋 {@link ResourceNotFoundException}。
     * 【概念】唯讀交易提示 JPA 可做讀取優化、避免意外寫入。
     *
     * @param id 訂單主鍵
     * @return 訂單回應
     */
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return orderMapper.toResponse(findOrThrow(id));
    }

    /**
     * 【職責】分頁查詢訂單（可選 symbol、status 篩選）。
     * 【技巧】夾緊 page／size；{@link PageRequest} + 依 createdAt DESC；Stream map 成 DTO。
     * 【概念】防禦性夾緊參數，避免前端傳異常值導致查詢失敗或一次撈太多。
     *
     * @param symbol 商品代碼（null 表示不篩選）
     * @param status 訂單狀態（null 表示不篩選）
     * @param page   頁碼（最小 0）
     * @param size   每頁筆數（1~100）
     * @return 分頁結果
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> list(String symbol, OrderStatus status, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        // 防禦性程式：即使前端傳入異常 page/size，也不會讓 Repository 查詢出錯
        Page<OrderEntity> result = orderRepository.search(symbol, status,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<OrderResponse> data = result.getContent().stream()
                .map(orderMapper::toResponse)
                .toList();
        return new PagedResponse<>(data,
                PagedResponse.PageMeta.of(safePage, safeSize, result.getTotalElements()));
    }

    /**
     * 【職責】更新訂單欄位與狀態。
     * 【技巧】先 findOrThrow，再覆寫欄位後 save。
     * 【概念】clientOrderId 不在更新請求中，建立後視為不可變冪等鍵。
     *
     * @param id      訂單主鍵
     * @param request 更新請求
     * @return 更新後訂單
     */
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        OrderEntity entity = findOrThrow(id);
        entity.setSymbol(request.getSymbol());
        entity.setSide(request.getSide());
        entity.setQuantity(request.getQuantity());
        entity.setPrice(request.getPrice());
        entity.setStatus(request.getStatus());
        return orderMapper.toResponse(orderRepository.save(entity));
    }

    /**
     * 【職責】刪除單筆訂單。
     * 【技巧】先確認存在再 delete，避免靜默成功。
     * 【概念】不存在時拋 404 語意例外，與「刪除成功」明確區分。
     *
     * @param id 訂單主鍵
     */
    public void delete(Long id) {
        OrderEntity entity = findOrThrow(id);
        orderRepository.delete(entity);
    }

    /**
     * 【職責】批次建立；單筆失敗不影響其他筆，結果彙總於 {@link BatchResult}。
     * 【技巧】{@link HashSet} 偵測批次內重複 clientOrderId；再查 DB 是否已存在。
     * 【概念】盡力而為策略：部分失敗仍保留成功筆，呼叫端依 succeeded／failed 決定後續。
     *
     * @param request 批次建立請求
     * @return 成功／失敗統計與明細
     */
    public BatchResult batchCreate(BatchCreateRequest request) {
        BatchResult result = new BatchResult();
        List<CreateOrderRequest> orders = request.getOrders();
        result.setRequested(orders.size());

        Set<String> seenInBatch = new HashSet<>();
        for (int i = 0; i < orders.size(); i++) {
            CreateOrderRequest order = orders.get(i);
            String reference = order.getClientOrderId();
            // 同一批次內重複的 clientOrderId 直接標記失敗，不寫入 DB
            if (!seenInBatch.add(reference)) {
                result.addFailure(i, reference, "duplicate clientOrderId within batch");
                continue;
            }
            if (orderRepository.existsByClientOrderId(reference)) {
                result.addFailure(i, reference, "clientOrderId already exists");
                continue;
            }
            OrderEntity saved = orderRepository.save(orderMapper.toEntity(order));
            result.addSuccess(saved.getId());
        }
        return result;
    }

    /**
     * 【職責】批次刪除；找不到的 ID 記錄於 failures，不拋例外。
     * 【技巧】逐筆 {@code existsById} 再 {@code deleteById}。
     * 【概念】與單筆 delete 不同：批次不因一筆不存在而中斷整批。
     *
     * @param request 批次刪除請求
     * @return 成功／失敗統計與明細
     */
    public BatchResult batchDelete(BatchDeleteRequest request) {
        BatchResult result = new BatchResult();
        List<Long> ids = request.getIds();
        result.setRequested(ids.size());

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            if (id != null && orderRepository.existsById(id)) {
                orderRepository.deleteById(id);
                result.addSuccess(id);
            } else {
                result.addFailure(i, id == null ? null : id.toString(), "order not found");
            }
        }
        return result;
    }

    /**
     * 查詢訂單或拋出 {@link ResourceNotFoundException}。
     *
     * @param id 訂單主鍵
     * @return 訂單 Entity
     */
    private OrderEntity findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND,
                        "Order not found: " + id));
    }
}
