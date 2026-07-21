package com.trading.crud.order.dto;

import com.trading.crud.order.domain.OrderSide;
import com.trading.crud.order.domain.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 【職責】更新訂單請求 DTO（PUT {@code /api/v1/orders/{id}}）。
 * 【技巧】Bean Validation；可更新 symbol／side／quantity／price／status。
 * 【概念】不含 clientOrderId——建立後視為不可變冪等鍵，避免改鍵造成對帳混亂。
 * 【邊界】不負責是否允許該狀態轉移（教學簡化，直接覆寫）。
 */
public class UpdateOrderRequest {

    /** 交易標的代碼 */
    @NotBlank(message = "symbol 不可為空")
    @Size(max = 20, message = "symbol 長度不可超過 20")
    private String symbol;

    /** 買賣方向 */
    @NotNull(message = "side 不可為空")
    private OrderSide side;

    /** 委託數量 */
    @NotNull(message = "quantity 不可為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "quantity 必須大於 0")
    private BigDecimal quantity;

    /** 委託價格 */
    @NotNull(message = "price 不可為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "price 必須大於 0")
    private BigDecimal price;

    /** 訂單狀態（可手動推進生命週期，教學用途） */
    @NotNull(message = "status 不可為空")
    private OrderStatus status;

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
}
