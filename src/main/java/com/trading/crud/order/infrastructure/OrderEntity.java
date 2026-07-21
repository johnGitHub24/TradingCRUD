package com.trading.crud.order.infrastructure;

import com.trading.crud.order.domain.OrderSide;
import com.trading.crud.order.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 【職責】訂單 JPA 實體，對應資料表 {@code orders}。
 * 【技巧】{@code @Entity}／{@code @Table}；金額用 {@link BigDecimal} + precision／scale；
 *         {@code @CreationTimestamp}／{@code @UpdateTimestamp} 自動時間戳。
 * 【概念】Entity 只做 ORM 映射；商業規則與 DTO 轉換在 Service／Mapper。
 *         BigDecimal 避免 double 浮點誤差，對應 DB DECIMAL。
 * 【邊界】不含查詢方法（見 {@link OrderRepository}）。
 */
@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity {

    /** 資料庫主鍵，自動遞增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客戶端自訂訂單 ID，用於冪等與對帳，全表唯一 */
    @Column(name = "client_order_id", unique = true, nullable = false, length = 64)
    private String clientOrderId;

    /** 交易標的代碼，例如 BTCUSDT */
    @Column(nullable = false, length = 20)
    private String symbol;

    /** 買賣方向（BUY / SELL） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private OrderSide side;

    /** 委託數量 */
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    /** 委託價格 */
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    /** 訂單狀態，新建時預設 NEW */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    /** 建立時間，插入時自動設定 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** 最後更新時間，每次 save 時自動更新 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
