package com.trading.crud.security;

import com.trading.crud.user.infrastructure.UserEntity;
import com.trading.crud.user.infrastructure.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【職責】Spring Security 使用者載入：依 username 查 {@link UserEntity} 轉為 {@link UserDetails}。
 * 【技巧】實作 {@link UserDetailsService}；權限字串為 {@code ROLE_} + 角色名。
 * 【概念】登入時 AuthenticationManager 呼叫此類取得密碼雜湊再 BCrypt 比對。
 *         JWT 請求不走此路徑，由 {@link JwtAuthenticationFilter} 直接還原身分。
 * 【邊界】不負責 JWT 解析、註冊新使用者 API。
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 建構子注入使用者 Repository。
     *
     * @param userRepository 帳號查詢介面
     */
    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 【職責】載入使用者以進行帳密認證。
     * 【技巧】查不到拋 {@link UsernameNotFoundException}；{@code disabled(!enabled)} 對應停用帳號。
     * 【概念】回傳的是 Spring Security 的 UserDetails，不是本專案的 UserEntity，以符合框架契約。
     *
     * @param username 登入帳號
     * @return 含密碼雜湊與 ROLE_ 權限的 UserDetails
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
