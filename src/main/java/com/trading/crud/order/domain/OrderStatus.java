package com.trading.crud.order.domain;

/**
 * 【職責】訂單生命週期狀態（NEW → 成交／取消／拒絕）。
 * 【技巧】enum 持久化於 Entity；更新時由 {@link com.trading.crud.order.dto.UpdateOrderRequest} 傳入。
 * 【概念】典型流轉 NEW → PARTIALLY_FILLED → FILLED，或 NEW → CANCELLED／REJECTED。
 *         本教學允許 PUT 直接設狀態；真實系統多由撮合引擎驅動。
 * 【邊界】不含狀態機強制轉移規則（教學簡化）。
 */
public enum OrderStatus {
    /** 新建，尚未成交 */
    NEW,
    /** 部分成交 */
    PARTIALLY_FILLED,
    /** 完全成交 */
    FILLED,
    /** 已取消 */
    CANCELLED,
    /** 已拒絕 */
    REJECTED
}
