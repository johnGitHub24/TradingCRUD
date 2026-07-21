package com.trading.crud.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 【職責】批次刪除訂單請求 DTO（DELETE {@code /api/v1/orders/batch}）。
 * 【技巧】{@code @NotEmpty} + {@code @Size(max = 500)} 限制 ID 清單。
 * 【概念】只傳主鍵清單即可刪除；不存在的 ID 會出現在 {@link BatchResult#getFailures()}。
 * 【邊界】不含刪除結果（由 Service 回傳 BatchResult）。
 */
public class BatchDeleteRequest {

    /** 要刪除的訂單主鍵清單；不存在的 ID 會記錄在 {@link BatchResult#getFailures()} */
    @NotEmpty(message = "ids 不可為空")
    @Size(max = 500, message = "單次批次最多 500 筆")
    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
