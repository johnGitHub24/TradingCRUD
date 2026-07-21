package com.trading.crud.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】JwtService 單元測試：簽發內容、竄改失效、不同密鑰失效。
 * 【技巧】手動 new JwtProperties／JwtService，不啟動 Spring。
 * 【概念】保護「無狀態 Token」核心：簽章與密鑰正確性。
 */
@Tag("unit")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-secret-key-with-at-least-32-bytes-long!!");
        properties.setExpirationMinutes(60);
        properties.setIssuer("trading-crud-test");
        jwtService = new JwtService(properties);
    }

    /**
     * CASE JWT-001：產生的 Token 可解析 subject 與 role。
     * Given: generateToken(admin, ADMIN)；When: isValid／extract；Then: 有效且欄位正確。
     */
    @Test
    void JWT_001_generateToken_carriesSubjectAndRole() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    /**
     * CASE JWT-002：竄改 Token 尾端 → isValid=false。
     * Given: 合法 Token 改最後兩字元；When: isValid；Then: false。
     * 【技巧驗證】簽章驗證失敗被吞掉回 false，不拋堆疊給呼叫端。
     */
    @Test
    void JWT_002_tamperedToken_isInvalid() {
        String token = jwtService.generateToken("admin", "ADMIN");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    /**
     * CASE JWT-003：不同 secret 簽出的 Token 對本服務無效。
     * Given: 另一把密鑰簽發；When: 本 jwtService.isValid；Then: false。
     */
    @Test
    void JWT_003_tokenSignedWithDifferentSecret_isInvalid() {
        JwtProperties other = new JwtProperties();
        other.setSecret("another-different-secret-key-32-bytes-minimum!!!");
        other.setExpirationMinutes(60);
        other.setIssuer("trading-crud-test");
        String foreignToken = new JwtService(other).generateToken("admin", "ADMIN");

        assertThat(jwtService.isValid(foreignToken)).isFalse();
    }
}
