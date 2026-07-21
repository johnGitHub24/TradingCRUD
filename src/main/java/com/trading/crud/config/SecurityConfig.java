package com.trading.crud.config;

import com.trading.crud.security.AdminProperties;
import com.trading.crud.security.JwtAuthenticationFilter;
import com.trading.crud.security.JwtProperties;
import com.trading.crud.security.RestAccessDeniedHandler;
import com.trading.crud.security.RestAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 【職責】Spring Security 核心設定：無狀態 JWT 過濾鏈、公開路徑、方法級授權、密碼編碼與認證管理器。
 * 【技巧】{@code @EnableMethodSecurity}、{@code SessionCreationPolicy.STATELESS}、
 *         {@code addFilterBefore(JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter)}。
 * 【概念】請求流程：JWT Filter 還原身分 → authorizeHttpRequests 判斷是否需登入 →
 *         {@code @PreAuthorize} 再查角色。STATELESS 表示不靠伺服器 Session，每次靠 JWT。
 * 【邊界】不負責登入 API 實作（AuthController）與 Token 簽發（JwtService）。
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class})
public class SecurityConfig {

    /** 無需 JWT 即可存取的路徑 */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/login",
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/h2-console/**"
    };

    /**
     * 【職責】組裝 HTTP 安全過濾鏈：關閉 CSRF、啟用 JWT 過濾器、設定 401／403 處理。
     * 【技巧】REST + JWT 通常關閉 CSRF；H2 Console 需 {@code frameOptions.sameOrigin}。
     * 【概念】公開路徑 permitAll，其餘 authenticated。未登入走 EntryPoint（401），
     *         已登入但權限不足走 AccessDeniedHandler（403）。
     *
     * @param http                        HttpSecurity 建構器
     * @param jwtAuthenticationFilter     JWT 請求過濾器
     * @param authenticationEntryPoint    未認證時 401 處理
     * @param accessDeniedHandler         權限不足時 403 處理
     * @return 已建置的 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           RestAuthenticationEntryPoint authenticationEntryPoint,
                                           RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // REST + JWT 通常關閉 CSRF（無 Cookie Session）；純 API 常見做法
                .csrf(AbstractHttpConfigurer::disable)
                // 允許 H2 Console 以 iframe 嵌入（僅開發環境使用）
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 在 Spring Security 預設帳密過濾器「之前」插入 JWT 解析
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 【職責】提供 BCrypt 密碼編碼器 Bean。
     * 【技巧】{@link BCryptPasswordEncoder}；登入比對與種子資料雜湊共用同一 Bean。
     * 【概念】BCrypt 內建 salt，相同明文每次雜湊結果不同，仍可比對成功。
     *
     * @return PasswordEncoder 實例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 【職責】帳密認證管理器（登入時由 {@link com.trading.crud.auth.AuthService} 使用）。
     * 【技巧】{@link DaoAuthenticationProvider} + {@link UserDetailsService} + PasswordEncoder。
     * 【概念】ProviderManager 委派 Provider 完成「查使用者＋比對密碼」；與 JWT 驗證路徑分離。
     *
     * @param userDetailsService 使用者載入服務
     * @param passwordEncoder    密碼比對編碼器
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }
}
