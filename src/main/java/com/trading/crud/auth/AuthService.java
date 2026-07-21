package com.trading.crud.auth;

import com.trading.crud.auth.dto.LoginRequest;
import com.trading.crud.auth.dto.LoginResponse;
import com.trading.crud.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * 【職責】認證商業邏輯：驗證帳密並簽發 JWT。
 * 【技巧】委派 {@link AuthenticationManager} 驗證帳密；成功後呼叫 {@link JwtService#generateToken}。
 * 【概念】登入流程：AuthenticationManager → 查 DB 比對 BCrypt → JwtService 簽發 Token。
 *         之後 API 不再送密碼，只送 JWT；Filter 負責驗證 Token。
 * 【邊界】不負責 HTTP 回應組裝（Controller）、使用者資料查詢（{@link com.trading.crud.security.AppUserDetailsService}）。
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * 建構子注入認證管理器與 JWT 服務，便於單元測試替換依賴。
     *
     * @param authenticationManager Spring Security 認證管理器
     * @param jwtService            JWT 簽發與解析服務
     */
    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * 【職責】登入主流程：驗證帳密、萃取角色、簽發 Token 並組裝回應。
     * 【技巧】{@code UsernamePasswordAuthenticationToken} 交給 AuthenticationManager；
     *         Stream 去掉 {@code ROLE_} 前綴後寫入 JWT claim，方便前端顯示。
     * 【概念】Spring Security 內部權限字串是 {@code ROLE_ADMIN}；對外 API 回傳 {@code ADMIN} 較易讀。
     *         失敗時拋 {@code BadCredentialsException}，由全域處理器轉 401。
     *
     * @param request 含 username、password
     * @return JWT 與過期秒數、角色
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Spring Security 權限字串為 ROLE_ADMIN；回傳給前端時去掉前綴，較易讀
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElse("USER");

        String token = jwtService.generateToken(authentication.getName(), role);
        return new LoginResponse(token, "Bearer", jwtService.expirationSeconds(),
                authentication.getName(), role);
    }
}
