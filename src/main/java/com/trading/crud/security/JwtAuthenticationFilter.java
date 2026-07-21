package com.trading.crud.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 【職責】JWT Bearer Token 請求過濾器：驗證後寫入 {@link SecurityContextHolder}。
 * 【技巧】繼承 {@link OncePerRequestFilter}；讀 {@code Authorization: Bearer ...}；
 *         建立 {@link UsernamePasswordAuthenticationToken}（權限加 {@code ROLE_} 前綴）。
 * 【概念】每個請求執行一次。無 Token 或無效時不在此擋下，由後續授權階段回 401。
 * 【邊界】不負責登入發 Token、403／401 JSON 組裝（EntryPoint／AccessDeniedHandler）。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /**
     * 建構子注入 JWT 服務。
     *
     * @param jwtService Token 驗證與解析
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * 【職責】每個請求執行一次：有合法 Bearer Token 時建立 Authentication。
     * 【技巧】{@code jwtService.isValid} 通過後寫入 SecurityContext；最後必須 {@code filterChain.doFilter}。
     * 【概念】Filter 只「還原身分」，是否允許存取由 authorizeHttpRequests／@PreAuthorize 決定。
     *
     * @param request     HTTP 請求
     * @param response    HTTP 回應
     * @param filterChain 過濾鏈
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            if (jwtService.isValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                // Spring Security 要求權限字串以 ROLE_ 開頭，與 @PreAuthorize("hasRole('ADMIN')") 對應
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // 無論是否帶 Token，都必須呼叫 doFilter 讓請求繼續往下傳
        filterChain.doFilter(request, response);
    }
}
