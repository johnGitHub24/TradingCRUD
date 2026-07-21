package com.trading.crud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 【職責】OpenAPI（Swagger UI）文件設定：標題、版本與 Bearer JWT 安全方案。
 * 【技巧】{@code @Bean OpenAPI}；{@code addSecurityItem} + {@code SecurityScheme.Type.HTTP} bearer。
 * 【概念】Swagger UI 預設要求帶 JWT；先登入取得 Token，再點 Authorize 貼上即可測受保護 API。
 * 【邊界】不負責個別端點的 {@code @Operation} 描述（由各 Controller 註解）。
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    /**
     * 【職責】組裝 OpenAPI 文件 Bean，含全域 JWT 安全需求。
     * 【技巧】springdoc 讀取此 Bean 產生 {@code /v3/api-docs} 與 Swagger UI。
     * 【概念】文件與程式碼同倉，避免 API 規格與實作漂移。
     *
     * @return 設定完成的 {@link OpenAPI} 實例
     */
    @Bean
    public OpenAPI tradingCrudOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradingCRUD API")
                        .version("0.1.0")
                        .description("JWT 認證 + Order CRUD/BATCH API"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
