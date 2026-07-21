package com.trading.crud.user.infrastructure;

import com.trading.crud.user.domain.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * 【職責】使用者 JPA 實體，對應資料表 {@code app_users}。
 * 【技巧】密碼存 BCrypt 雜湊於 {@code passwordHash}；角色 {@code @Enumerated(STRING)}。
 * 【概念】Entity＝資料庫一列的物件映射。登入由 {@link com.trading.crud.security.AppUserDetailsService} 載入。
 * 【邊界】不含註冊／改密 API（本專案以 DataSeeder 種子為主）。
 */
@Getter
@Setter
@Entity
@Table(name = "app_users")
public class UserEntity {

    /** 資料庫主鍵，自動遞增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登入帳號，全表唯一 */
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** BCrypt 雜湊後的密碼，絕不儲存明文 */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** 使用者角色（ADMIN / USER），以字串形式存入資料庫 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    /** 是否啟用；false 時無法登入 */
    @Column(nullable = false)
    private boolean enabled = true;

    /** 帳號建立時間，由 Hibernate 自動填入 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
