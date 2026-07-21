package com.trading.crud.order.domain;

/**
 * 【職責】訂單買賣方向（BUY／SELL）。
 * 【技巧】Java enum；JPA {@code @Enumerated(STRING)} 與 Jackson 反序列化共用。
 * 【概念】用 enum 而非字串常數，可在編譯期與反序列化時擋下非法值。
 * 【邊界】不含價格／數量語意，僅表示方向。
 */
public enum OrderSide {
    /** 買進 */
    BUY,
    /** 賣出 */
    SELL
}
