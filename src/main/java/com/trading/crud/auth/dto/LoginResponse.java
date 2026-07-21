package com.trading.crud.auth.dto;

/**
 * 【職責】登入成功回應 DTO：回傳 JWT、過期秒數與使用者基本資訊。
 * 【技巧】由 {@link com.trading.crud.auth.AuthService#login} 組裝；前端後續請求放
 *         {@code Authorization: Bearer <accessToken>}。
 * 【概念】Token 類型固定為 Bearer（OAuth2 慣例）。角色不含 {@code ROLE_} 前綴，方便 UI 顯示。
 * 【邊界】不負責 Token 驗證或刷新（本專案無 refresh token）。
 */
public class LoginResponse {

    /** JWT 存取權杖；後續 API 請求放在 Header：{@code Authorization: Bearer <token>} */
    private String accessToken;
    /** Token 類型，固定為 {@code Bearer}（OAuth2 慣例） */
    private String tokenType;
    /** Token 有效秒數；前端可用來決定何時重新登入 */
    private long expiresIn;
    /** 登入成功的使用者帳號 */
    private String username;
    /** 使用者角色（ADMIN 或 USER，不含 ROLE_ 前綴） */
    private String role;

    /** 預設建構子（JSON 反序列化用）。 */
    public LoginResponse() {
    }

    /**
     * 建立完整登入回應。
     *
     * @param accessToken  JWT 字串
     * @param tokenType    Token 類型（通常為 {@code Bearer}）
     * @param expiresIn    過期秒數
     * @param username     登入帳號
     * @param role         角色名稱（不含 {@code ROLE_} 前綴）
     */
    public LoginResponse(String accessToken, String tokenType, long expiresIn, String username, String role) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.username = username;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
