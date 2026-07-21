package com.trading.crud.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 【職責】預設管理員帳密設定（{@code app.admin.*}），供 DataSeeder 建立種子 ADMIN。
 * 【技巧】{@code @ConfigurationProperties(prefix = "app.admin")} 綁定 yml。
 * 【概念】僅用於「首次啟動、尚無帳號」的開發便利；正式環境應用環境變數覆寫並盡快改密。
 * 【邊界】不負責登入驗證本身。
 */
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    /** 種子管理員帳號，預設 admin */
    private String username = "admin";
    /** 種子管理員密碼，預設 admin123 */
    private String password = "admin123";

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
