package com.trading.crud.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 【職責】全域 REST 例外處理：將各類例外統一轉為 {@code application/problem+json} 與對應 HTTP 狀態碼。
 * 【技巧】{@code @RestControllerAdvice} + {@code @ExceptionHandler}；回應遵循 RFC 7807 Problem Details。
 * 【概念】避免每個 Controller 方法都寫 try-catch；前端可依 {@code errorCode}／{@code status} 統一處理錯誤。
 * 【邊界】不決定業務層拋出何種例外（由 Service 決定）；過濾鏈內 401／403 另見 Security Handler。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 【職責】處理 {@code @Valid} 驗證失敗，回 400 與欄位錯誤清單。
     * 【技巧】從 {@link MethodArgumentNotValidException} 取出 {@link FieldError} 轉成 errors 陣列。
     * 【概念】驗證失敗屬「請求格式／約束」問題，不是業務衝突（409）或找不到（404）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed", request.getRequestURI());
        body.put("errorCode", ErrorCodes.VALIDATION_FAILED);
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        body.put("errors", errors);
        return problemResponse(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * 【職責】處理資源不存在，回 404。
     * 【技巧】讀取例外內的 {@code errorCode}（如 ORDER_NOT_FOUND）。
     * 【概念】領域「找不到」與 HTTP 404 對齊，前端可直接顯示「資源不存在」。
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex,
                                                              HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request.getRequestURI());
        body.put("errorCode", ex.getErrorCode());
        return problemResponse(HttpStatus.NOT_FOUND, body);
    }

    /**
     * 【職責】處理資源衝突（重複），回 409。
     * 【技巧】對應 {@link DuplicateResourceException}（如重複 clientOrderId）。
     * 【概念】409 表示「請求語法正確，但與現有資源狀態衝突」，與 400 驗證失敗不同。
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex,
                                                               HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), request.getRequestURI());
        body.put("errorCode", ex.getErrorCode());
        return problemResponse(HttpStatus.CONFLICT, body);
    }

    /**
     * 【職責】處理登入帳密錯誤，回 401。
     * 【技巧】攔截 {@link BadCredentialsException}（AuthenticationManager 驗證失敗）。
     * 【概念】與「未帶 Token」的 401 共用狀態碼，但 errorCode 為 BAD_CREDENTIALS 以便前端區分。
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex,
                                                                    HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.UNAUTHORIZED, "Bad Credentials", "Invalid username or password", request.getRequestURI());
        body.put("errorCode", ErrorCodes.BAD_CREDENTIALS);
        return problemResponse(HttpStatus.UNAUTHORIZED, body);
    }

    /**
     * 【職責】處理已認證但權限不足，回 403。
     * 【技巧】攔截 {@link AccessDeniedException}（例如 {@code @PreAuthorize} 失敗且進入 MVC 例外路徑）。
     * 【概念】403＝「我知道你是誰，但你不夠權限」；與 401「你是誰？」不同。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex,
                                                                  HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.FORBIDDEN, "Forbidden", "Access denied", request.getRequestURI());
        body.put("errorCode", ErrorCodes.FORBIDDEN);
        return problemResponse(HttpStatus.FORBIDDEN, body);
    }

    /**
     * 【職責】處理不支援的 HTTP 方法，回 405。
     * 【技巧】攔截 {@link HttpRequestMethodNotSupportedException}。
     * 【概念】例如對只支援 GET 的路徑送 POST；與「路徑不存在」的 404 不同。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                        HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), request.getRequestURI());
        body.put("errorCode", ErrorCodes.METHOD_NOT_ALLOWED);
        return problemResponse(HttpStatus.METHOD_NOT_ALLOWED, body);
    }

    /**
     * 【職責】兜底處理未預期例外，回 500。
     * 【技巧】最寬鬆的 {@code Exception.class} 必須放在最後，避免搶走更具體的 Handler。
     * 【概念】對外只回「Unexpected error」，避免把堆疊細節洩漏給客戶端。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = problemBody(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error", request.getRequestURI());
        body.put("errorCode", ErrorCodes.INTERNAL_ERROR);
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, body);
    }

    private ResponseEntity<Map<String, Object>> problemResponse(HttpStatus status, Map<String, Object> body) {
        return ResponseEntity.status(status)
                .header("Content-Type", "application/problem+json")
                .body(body);
    }

    private Map<String, Object> problemBody(HttpStatus status, String title, String detail, String instance) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", instance);
        return body;
    }

    private Map<String, String> toFieldError(FieldError error) {
        return Map.of("field", error.getField(),
                "message", error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage());
    }
}
