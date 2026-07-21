package com.trading.crud.order.dto;

import com.trading.crud.order.domain.OrderSide;
import com.trading.crud.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 【職責】訂單 API 回應 DTO；由 Mapper 組裝，不直接暴露 Entity。
 * 【技巧】含伺服器產生的 id、status、時間戳；Jackson 序列化為 JSON。
 * 【概念】回應與請求分離：建立請求無 id，回應才有；避免把 JPA 代理物件直接回傳。
 * 【邊界】不含內部實作細節（如 password、懶加載關聯——本實體亦無）。
 */
public class OrderResponse {

    /** 伺服器產生的訂單主鍵 */
    private Long id;
    /** 客戶端自訂訂單 ID */
    private String clientOrderId;
    /** 交易標的代碼 */
    private String symbol;
    /** 買賣方向 */
    private OrderSide side;
    /** 委託數量 */
    private BigDecimal quantity;
    /** 委託價格 */
    private BigDecimal price;
    /** 目前訂單狀態 */
    private OrderStatus status;
    /** 建立時間（含時區偏移） */
    private OffsetDateTime createdAt;
    /** 最後更新時間 */
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
