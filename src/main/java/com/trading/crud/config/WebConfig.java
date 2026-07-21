package com.trading.crud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 【職責】Web MVC 設定：CORS 允許前端跨域呼叫 API。
 * 【技巧】實作 {@link WebMvcConfigurer#addCorsMappings}；來源來自 {@code app.cors.allowed-origins}。
 * 【概念】瀏覽器同源政策會擋「前端 :5173 呼叫後端 :8083」；CORS 告訴瀏覽器哪些來源可跨域。
 *         這與 JWT 認證無關，兩者需分開設定。
 * 【邊界】不負責認證、路由、靜態資源（由 SecurityConfig 與 Spring Boot 預設處理）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    /**
     * 【職責】設定 {@code /api/**} 的 CORS 映射。
     * 【技巧】{@link CorsRegistry}：allowedOrigins／Methods／Headers、{@code allowCredentials(true)}。
     * 【概念】開發時 Vite 與後端不同埠，沒有 CORS 前端會在瀏覽器直接失敗（後端可能仍收到 preflight）。
     *
     * @param registry Spring MVC CORS 註冊器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
