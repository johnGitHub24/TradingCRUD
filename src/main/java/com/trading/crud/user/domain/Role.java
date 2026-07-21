package com.trading.crud.user.domain;

/**
 * 【職責】應用程式使用者角色（ADMIN／USER）。
 * 【技巧】enum 持久化於 UserEntity；Spring Security 權限為 {@code ROLE_} + 列舉名。
 * 【概念】{@code hasRole('ADMIN')} 比對 {@code ROLE_ADMIN}。USER 可讀訂單，ADMIN 可寫入／批次。
 * 【邊界】不含細粒度權限（如欄位級 ACL）。
 */
public enum Role {
    /** 管理員：可執行訂單寫入與批次操作 */
    ADMIN,
    /** 一般使用者：可讀取訂單 */
    USER
}
