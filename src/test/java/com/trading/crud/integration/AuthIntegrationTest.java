package com.trading.crud.integration;

import com.trading.crud.support.CrudTestFixtures;
import com.trading.crud.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】認證 API 整合測試：登入成功／失敗、驗證失敗、/me 需 Token。
 * 【技巧】MockMvc + 測試資料 JSON（CrudTestFixtures）；繼承 {@link IntegrationTestBase}。
 * 【概念】走完整 Security 過濾鏈與 DataSeeder 種子 admin，驗證 JWT 登入契約。
 */
class AuthIntegrationTest extends IntegrationTestBase {

    /**
     * CASE AUTH-001 / JWT-001 / USER-001：合法 admin 登入回 200 + accessToken。
     * Given: 正確帳密；When: POST /api/v1/auth/login；Then: 200、Bearer、role=ADMIN。
     */
    @Test
    void AUTH_001_adminLogin_returnsToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("auth", "AUTH-001-SUCCESS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    /**
     * CASE AUTH-002 / USER-002：錯誤密碼回 401 BAD_CREDENTIALS。
     * Given: 錯誤密碼；When: POST login；Then: 401 + errorCode。
     */
    @Test
    void AUTH_002_badCredentials_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("auth", "AUTH-002-BAD_CREDENTIALS")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("BAD_CREDENTIALS"));
    }

    /**
     * CASE AUTH-003：缺必填欄位回 400 VALIDATION_FAILED。
     * Given: 不完整 body；When: POST login；Then: 400。
     */
    @Test
    void AUTH_003_missingRequired_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("auth", "AUTH-003-MISSING_REQUIRED")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    /**
     * CASE AUTH-004 / JWT-001：帶合法 Token 查 /me 回 username。
     * Given: adminToken；When: GET /api/v1/auth/me；Then: 200 + username=admin。
     * 【技巧驗證】Authentication 由 JWT Filter 還原，Controller 不需解析 Header。
     */
    @Test
    void AUTH_004_me_withToken_returnsUsername() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    /**
     * CASE AUTH-005 / SEC-001：無 Token 查 /me → 401 UNAUTHORIZED。
     * Given: 無 Authorization；When: GET /api/v1/auth/me；Then: 401。
     */
    @Test
    void AUTH_005_SEC_001_me_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }
}
