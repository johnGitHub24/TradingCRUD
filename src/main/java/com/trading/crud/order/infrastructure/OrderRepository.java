package com.trading.crud.order.infrastructure;

import com.trading.crud.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 【職責】訂單 Spring Data JPA Repository：依 clientOrderId 查詢與可選篩選的分頁搜尋。
 * 【技巧】繼承 {@link JpaRepository}；衍生查詢方法名；{@link #search} 用 JPQL 可選條件。
 * 【概念】{@code :symbol IS NULL OR ...} 讓同一方法支援「有篩選／不篩選」。
 *         批次邏輯與唯一性業務判斷在 Service，不在 Repository。
 * 【邊界】不含商業規則編排。
 */
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * 【職責】依客戶端訂單 ID 查詢單筆。
     * 【技巧】Spring Data 方法名衍生查詢。
     * 【概念】回傳 Optional，呼叫端決定「找不到」時要拋例外或回空。
     */
    Optional<OrderEntity> findByClientOrderId(String clientOrderId);

    /**
     * 【職責】檢查 clientOrderId 是否已存在。
     * 【技巧】{@code existsBy...} 衍生查詢，只回布林、不載入整列。
     * 【概念】建立前檢查比「先 insert 再抓 unique 違規」更易回傳清楚錯誤碼。
     */
    boolean existsByClientOrderId(String clientOrderId);

    /**
     * 【職責】批次查詢多個 clientOrderId。
     * 【技巧】{@code findBy...In} 對應 SQL IN。
     * 【概念】供進階批次場景一次撈多筆，避免 N 次單筆查詢。
     */
    List<OrderEntity> findByClientOrderIdIn(List<String> clientOrderIds);

    /**
     * 【職責】分頁搜尋；symbol、status 為 null 時不套用該條件。
     * 【技巧】{@code @Query} JPQL + {@link Pageable}。
     * 【概念】可選參數用 IS NULL 短路，比動態 Criteria 字串拼接更安全、可讀。
     */
    @Query("""
            SELECT o FROM OrderEntity o
            WHERE (:symbol IS NULL OR o.symbol = :symbol)
              AND (:status IS NULL OR o.status = :status)
            """)
    Page<OrderEntity> search(@Param("symbol") String symbol,
                             @Param("status") OrderStatus status,
                             Pageable pageable);
}
