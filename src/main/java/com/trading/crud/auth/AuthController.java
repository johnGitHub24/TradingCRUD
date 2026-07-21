package com.trading.crud.auth;

import com.trading.crud.auth.dto.LoginRequest;
import com.trading.crud.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 【職責】認證 API 入口：接收登入參數、轉交 {@link AuthService}、回傳 JWT 或目前使用者資訊。
 * 【技巧】薄 Controller：{@code @RestController} + {@code @Valid}；基底路徑 {@code /api/v1/auth}。
 * 【概念】Controller 像「櫃檯」只收送 HTTP；Service 像「後台」處理商業邏輯。測試時可 Mock Service，
 *         不必啟動整個 Web 容器。
 * 【邊界】不負責密碼比對、Token 簽章（由 Service／{@link com.trading.crud.security.JwtService} 處理）。
 */
@Tag(name = "Auth", description = "JWT 登入與目前使用者資訊")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /** 建構子注入 Service，保持 Controller 無狀態、可測試。 */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 【職責】使用者登入，成功回傳 Bearer JWT。
     * 【技巧】{@code @PostMapping("/login")} + {@code @Valid LoginRequest}；錯誤由全域例外處理器轉 HTTP。
     * 【概念】登入成功後前端只帶 Token，不再送密碼。錯誤密碼 → 401；欄位驗證失敗 → 400。
     *
     * @param request 帳密
     * @return JWT 與角色資訊
     */
    @Operation(summary = "登入取得 JWT", description = "使用 username/password 換取 accessToken")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 【職責】查詢目前登入使用者（需帶 Authorization: Bearer Token）。
     * 【技巧】方法參數注入 {@link Authentication}；由 {@link com.trading.crud.security.JwtAuthenticationFilter}
     *         從 JWT 解析後放入 SecurityContext，Controller 不必手動解析 Header。
     * 【概念】「誰在呼叫」由 Security 過濾鏈還原，業務層只讀 {@code authentication.getName()}。
     *
     * @param authentication 目前安全上下文中的身分
     * @return username 與 authorities
     */
    @Operation(summary = "目前使用者", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities());
    }
}
