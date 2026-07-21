package com.trading.crud.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 【職責】JWT 外部化設定（{@code app.jwt.*}）：secret、過期分鐘、issuer。
 * 【技巧】{@code @ConfigurationProperties}；由 SecurityConfig {@code @EnableConfigurationProperties} 啟用。
 * 【概念】改 secret／過期時間不需改程式，重啟即可；正式環境密鑰務必用環境變數注入。
 * 【邊界】不含簽發／驗證邏輯（見 {@link JwtService}）。
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HMAC 簽章密鑰（必填） */
    private String secret;
    /** Token 有效分鐘數，預設 120 */
    private long expirationMinutes = 120;
    /** JWT issuer claim，預設 trading-crud */
    private String issuer = "trading-crud";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
