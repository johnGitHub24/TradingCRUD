package com.trading.crud.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 【職責】批次建立訂單請求 DTO（POST {@code /api/v1/orders/batch}）。
 * 【技巧】{@code @Valid} 巢狀驗證每筆 {@link CreateOrderRequest}；{@code @Size(max = 500)} 限制單次筆數。
 * 【概念】巢狀 {@code @Valid} 讓「外層清單＋內層欄位」一次驗證，避免只檢查清單非空卻漏掉單筆欄位。
 * 【邊界】不含批次結果彙總（見 {@link BatchResult}）。
 */
public class BatchCreateRequest {

    /** 要批次建立的訂單清單；每筆套用 {@link CreateOrderRequest} 的驗證規則 */
    @NotEmpty(message = "orders 不可為空")
    @Size(max = 500, message = "單次批次最多 500 筆")
    @Valid
    private List<CreateOrderRequest> orders;

    public List<CreateOrderRequest> getOrders() {
        return orders;
    }

    public void setOrders(List<CreateOrderRequest> orders) {
        this.orders = orders;
    }
}
