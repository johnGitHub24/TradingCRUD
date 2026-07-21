package com.trading.crud.security;

import com.trading.crud.support.IntegrationTestBase;
import com.trading.crud.user.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】安全整合測試：無 Token 401、USER 寫入 403、USER 讀取允許、無效 Token 401。
 * 【技巧】MockMvc + ensureUser 種子一般使用者；驗證 @PreAuthorize 與 EntryPoint。
 * 【概念】區分 401（未認證）與 403（已認證但權限不足）。
 */
class SecurityIntegrationTest extends IntegrationTestBase {

    private static final String NEW_ORDER = """
            {"clientOrderId":"sec-1","symbol":"BTCUSDT","side":"BUY","quantity":0.5,"price":65000.00}
            """;

    @BeforeEach
    void seedRegularUser() {
        ensureUser("trader", "trader123", Role.USER);
    }

    /**
     * CASE SEC-001：無 Token 存取受保護端點 → 401 UNAUTHORIZED。
     * Given: 無 Authorization；When: GET /orders；Then: 401。
     */
    @Test
    void SEC_001_noToken_protectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    /**
     * CASE SEC-002：USER 角色寫入 → 403 FORBIDDEN。
     * Given: trader JWT；When: POST /orders；Then: 403。
     * 【技巧驗證】@PreAuthorize("hasRole('ADMIN')") 擋下非 ADMIN。
     */
    @Test
    void SEC_002_userRole_writeEndpoint_returns403() throws Exception {
        String token = loginAndGetToken("trader", "trader123");

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_ORDER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    /**
     * CASE SEC-003：USER 角色讀取允許。
     * Given: trader JWT；When: GET /orders；Then: 200。
     */
    @Test
    void SEC_003_userRole_readEndpoint_isAllowed() throws Exception {
        String token = loginAndGetToken("trader", "trader123");

        mockMvc.perform(get("/api/v1/orders").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    /**
     * CASE SEC-004：無效 Token → 401。
     * Given: Bearer not-a-real-token；When: GET /orders；Then: 401。
     */
    @Test
    void SEC_004_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
