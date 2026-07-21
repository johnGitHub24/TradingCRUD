package com.trading.crud.order;

import com.trading.crud.order.dto.CreateOrderRequest;
import com.trading.crud.order.dto.OrderResponse;
import com.trading.crud.order.domain.OrderStatus;
import com.trading.crud.order.infrastructure.OrderEntity;
import org.springframework.stereotype.Component;

/**
 * 【職責】訂單 Entity 與 DTO 轉換；新建訂單預設狀態為 {@link OrderStatus#NEW}。
 * 【技巧】獨立 {@code @Component} Mapper，集中欄位對應。
 * 【概念】把「對外 JSON（DTO）」與「資料庫結構（Entity）」分離，避免 JPA 註解洩漏到 API。
 * 【邊界】不負責驗證、持久化、商業規則判斷。
 */
@Component
public class OrderMapper {

    /**
     * 【職責】將建立請求轉為尚未持久化的 JPA Entity。
     * 【技巧】手動欄位對應；狀態固定設為 NEW。
     * 【概念】狀態流轉由更新 API 處理，建立時不接受客戶端指定 status，避免跳過生命週期。
     *
     * @param request 建立請求
     * @return 新訂單 Entity
     */
    public OrderEntity toEntity(CreateOrderRequest request) {
        OrderEntity entity = new OrderEntity();
        entity.setClientOrderId(request.getClientOrderId());
        entity.setSymbol(request.getSymbol());
        entity.setSide(request.getSide());
        entity.setQuantity(request.getQuantity());
        entity.setPrice(request.getPrice());
        // 新建訂單一律從 NEW 狀態開始；狀態流轉由更新 API 處理
        entity.setStatus(OrderStatus.NEW);
        return entity;
    }

    /**
     * 【職責】將 Entity 轉為 API 回應 DTO。
     * 【技巧】含 id、時間戳等持久化後才有的欄位。
     * 【概念】回傳 DTO 而非 Entity，可控制序列化欄位、避免懶加載意外。
     *
     * @param entity 已持久化訂單
     * @return 對外回應物件
     */
    public OrderResponse toResponse(OrderEntity entity) {
        OrderResponse response = new OrderResponse();
        response.setId(entity.getId());
        response.setClientOrderId(entity.getClientOrderId());
        response.setSymbol(entity.getSymbol());
        response.setSide(entity.getSide());
        response.setQuantity(entity.getQuantity());
        response.setPrice(entity.getPrice());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
