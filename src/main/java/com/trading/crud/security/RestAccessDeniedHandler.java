package com.trading.crud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.crud.common.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 【職責】已認證但權限不足時回 403，寫入 problem+json。
 * 【技巧】實作 {@link AccessDeniedHandler}；與 {@link com.trading.crud.common.GlobalExceptionHandler} 格式對齊。
 * 【概念】例如 USER 呼叫需 ADMIN 的 POST／orders。JWT 有效但 {@code @PreAuthorize} 失敗走此路徑。
 * 【邊界】不處理「未登入」（見 {@link RestAuthenticationEntryPoint}）。
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 建構子注入 JSON 序列化器。
     *
     * @param objectMapper Spring 管理的 ObjectMapper
     */
    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 【職責】回傳 403 Forbidden 與 {@link ErrorCodes#FORBIDDEN}。
     * 【技巧】直接寫 response writer（過濾鏈階段未必進入 MVC Advice）。
     * 【概念】Security 過濾鏈內的拒絕存取需自訂 Handler，才能與 API 錯誤格式一致。
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/problem+json");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", "Forbidden");
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("detail", "Access denied");
        body.put("instance", request.getRequestURI());
        body.put("errorCode", ErrorCodes.FORBIDDEN);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
