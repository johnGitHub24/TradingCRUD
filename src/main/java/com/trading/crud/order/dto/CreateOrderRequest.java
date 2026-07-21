package com.trading.crud.order.dto;

import com.trading.crud.order.domain.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 【職責】建立單筆訂單請求 DTO（POST {@code /api/v1/orders} 與批次元素）。
 * 【技巧】Bean Validation：{@code @NotBlank}／{@code @NotNull}／{@code @DecimalMin}／{@code @Size}。
 * 【概念】新建狀態由 Mapper 設為 NEW，請求不帶 status，避免客戶端跳過生命週期。
 * 【邊界】不含 id、時間戳（由伺服器產生）。
 */
public class CreateOrderRequest {

    /** 客戶端自訂唯一鍵，防止重複下單；對應 orders.client_order_id */
    @NotBlank(message = "clientOrderId 不可為空")
    @Size(max = 64, message = "clientOrderId 長度不可超過 64")
    private String clientOrderId;

    /** 交易標的代碼，例如 BTCUSDT */
    @NotBlank(message = "symbol 不可為空")
    @Size(max = 20, message = "symbol 長度不可超過 20")
    private String symbol;

    /** 買賣方向：BUY（買進）或 SELL（賣出） */
    @NotNull(message = "side 不可為空")
    private OrderSide side;

    /** 委託數量，必須大於 0 */
    @NotNull(message = "quantity 不可為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "quantity 必須大於 0")
    private BigDecimal quantity;

    /** 委託價格，必須大於 0 */
    @NotNull(message = "price 不可為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "price 必須大於 0")
    private BigDecimal price;

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
}
