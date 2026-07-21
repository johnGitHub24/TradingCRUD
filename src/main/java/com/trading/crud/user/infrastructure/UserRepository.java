package com.trading.crud.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 【職責】使用者 Spring Data JPA Repository：帳號查詢與存在檢查。
 * 【技巧】繼承 {@link JpaRepository}；方法名 {@code findByUsername}／{@code existsByUsername} 自動產生 SQL。
 * 【概念】供 AppUserDetailsService 登入載入、DataSeeder 種子判斷；無需手寫實作類。
 * 【邊界】不含密碼雜湊（由 PasswordEncoder 處理）。
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 【職責】依帳號查詢使用者。
     * 【技巧】衍生查詢回傳 Optional。
     * 【概念】找不到時由呼叫端轉成 UsernameNotFoundException 或業務例外。
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 【職責】檢查帳號是否已存在。
     * 【技巧】{@code existsBy...} 只回布林。
     * 【概念】種子資料與註冊前檢查常用，避免重複 insert。
     */
    boolean existsByUsername(String username);
}
