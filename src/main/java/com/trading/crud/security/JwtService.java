package com.trading.crud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 【職責】JWT 簽發與驗證：產生 Token、解析 subject／role、檢查過期與 issuer。
 * 【技巧】jjwt：{@code Jwts.builder()}／{@code parser().verifyWith()}；HMAC 金鑰來自 {@link JwtProperties}。
 * 【概念】Token = Header.Payload.Signature。伺服器無狀態：不存 Session，每次靠簽章驗證真偽。
 * 【邊界】不讀 HTTP Header（Filter 負責）；不驗證帳密（AuthService 負責）。
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    /**
     * 建構子注入設定並衍生 HMAC 簽章金鑰。
     *
     * @param properties JWT 外部化設定（secret、過期時間、issuer）
     */
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // 將設定檔中的 secret 字串轉成 HMAC-SHA 簽章金鑰（登入與驗證共用同一把鑰）
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 【職責】為已驗證使用者簽發 JWT。
     * 【技巧】寫入 subject、role claim、issuer、issuedAt、expiration，再 {@code signWith}。
     * 【概念】role 不含 ROLE_ 前綴；Filter 還原時再補上，與 hasRole 對齊。
     *
     * @param username 主體（subject）
     * @param role     角色名稱（不含 {@code ROLE_}）
     * @return 緊湊 JWT 字串
     */
    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * 【職責】回傳 Token 有效秒數（供 LoginResponse expiresIn）。
     * 【技巧】分鐘 × 60。
     * 【概念】前端可用此值決定何時提示重新登入。
     */
    public long expirationSeconds() {
        return properties.getExpirationMinutes() * 60;
    }

    /**
     * 【職責】從 Token 解析使用者名稱（subject）。
     * 【技巧】{@link #parse} 後取 {@code getSubject()}。
     * 【概念】subject 是 JWT 標準欄位，慣例放帳號。
     */
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * 【職責】從 Token 解析角色 claim。
     * 【技巧】自訂 claim {@code role}。
     * 【概念】角色放 claim 可讓 Filter 不必再查 DB（無狀態權限）。
     */
    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    /**
     * 【職責】驗證 Token 簽章、issuer 與是否未過期。
     * 【技巧】try／catch 吞掉解析例外，回 false；成功再比對 expiration。
     * 【概念】不向外拋細節，避免攻擊者依錯誤訊息探測簽章／issuer。
     *
     * @return 有效為 true；解析失敗或已過期為 false
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parse(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            // 簽章錯誤、過期、issuer 不符等都視為無效 Token，不向外拋出細節（避免資訊洩漏）
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
