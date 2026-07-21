package com.trading.crud.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 【職責】登入請求 DTO：承載 POST {@code /api/v1/auth/login} 的 JSON body。
 * 【技巧】Jakarta Bean Validation：{@code @NotBlank}；失敗時由 {@code @Valid} 觸發 400。
 * 【概念】DTO 只描述「輸入契約」，不含 Token 或角色。與 Entity 分離可避免把密碼雜湊欄位暴露給 API。
 * 【邊界】不負責密碼雜湊或 JWT 簽發。
 */
public class LoginRequest {

    /** 登入帳號（對應資料表 app_users.username） */
    @NotBlank(message = "username 不可為空")
    private String username;

    /** 登入密碼（明文傳輸僅限 HTTPS/開發環境；後端以 BCrypt 比對雜湊值） */
    @NotBlank(message = "password 不可為空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
