package com.trading.crud.auth;

import com.trading.crud.auth.dto.LoginRequest;
import com.trading.crud.auth.dto.LoginResponse;
import com.trading.crud.security.JwtService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 【職責】AuthService 單元測試：登入成功簽發 JWT、錯誤帳密向上拋出。
 * 【技巧】Mock AuthenticationManager 與 JwtService，不啟動 Security Filter。
 * 【概念】與 AUTH-001 and AUTH-002 整合層同一契約：成功有 Token；失敗是 BadCredentials。
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    /**
     * CASE AUTH-001：合法認證後回傳 Bearer Token 與去掉 ROLE_ 的角色。
     * Given: AuthenticationManager 回傳 ROLE_ADMIN；When: login；Then: token／role／expiresIn。
     */
    @Test
    void AUTH_001_login_returnsBearerTokenAndRole() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        Authentication authenticated = new UsernamePasswordAuthenticationToken(
                "admin",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(jwtService.generateToken("admin", "ADMIN")).thenReturn("signed.jwt.token");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    /**
     * CASE AUTH-002：帳密錯誤時 BadCredentialsException 不在 Service 被吞掉。
     * Given: authenticate 拋 BadCredentialsException；When: login；Then: 同一例外向上傳。
     */
    @Test
    void AUTH_002_badCredentials_propagates() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
