package com.trading.crud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.crud.common.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 【職責】未認證或 Token 無效時回 401，寫入 problem+json。
 * 【技巧】實作 {@link AuthenticationEntryPoint}；掛在 SecurityFilterChain。
 * 【概念】401＝「你是誰？我不知道」；403＝「我知道你是誰，但你不夠權限」。
 *         登入帳密錯誤則由 GlobalExceptionHandler 處理（BAD_CREDENTIALS）。
 * 【邊界】不處理已認證但權限不足（見 {@link RestAccessDeniedHandler}）。
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 建構子注入 JSON 序列化器。
     *
     * @param objectMapper Spring 管理的 ObjectMapper
     */
    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 【職責】回傳 401 Unauthorized 與 {@link ErrorCodes#UNAUTHORIZED}。
     * 【技巧】過濾鏈階段直接寫 JSON，格式與全域例外處理器一致。
     * 【概念】前端可依 401 導向登入頁，依 403 顯示「權限不足」。
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", "Unauthorized");
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("detail", "Authentication required");
        body.put("instance", request.getRequestURI());
        body.put("errorCode", ErrorCodes.UNAUTHORIZED);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
