package com.trading.crud.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.crud.user.domain.Role;
import com.trading.crud.user.infrastructure.UserEntity;
import com.trading.crud.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】整合測試基底：啟動完整 Spring Boot、MockMvc、test profile，並提供登入／種子輔助方法。
 * 【技巧】{@code @SpringBootTest} + {@code @AutoConfigureMockMvc} + {@code @ActiveProfiles("test")}。
 * 【概念】子類專注寫 CASE；共用登入取 Token、ensureUser，避免每個測試重複樣板。
 * 【邊界】不含具體業務斷言（由子類測試方法負責）。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * 【職責】以帳密登入並回傳 accessToken。
     * 【技巧】MockMvc POST /auth/login 後解析 JSON。
     * 【概念】整合測試走真實登入路徑，確保 JWT 與 Security 設定一致。
     */
    protected String loginAndGetToken(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("accessToken").asText();
    }

    /** 取得種子 admin 的 JWT（DataSeeder 預設帳密）。 */
    protected String adminToken() throws Exception {
        return loginAndGetToken("admin", "admin123");
    }

    /** 組成 Authorization Bearer 標頭值。 */
    protected String bearer(String token) {
        return "Bearer " + token;
    }

    /**
     * 【職責】若不存在則建立測試使用者（含 BCrypt 雜湊）。
     * 【技巧】existsByUsername 短路；PasswordEncoder.encode。
     * 【概念】安全測試需要非 ADMIN 角色時使用，避免污染種子 admin。
     */
    protected void ensureUser(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);
    }
}
