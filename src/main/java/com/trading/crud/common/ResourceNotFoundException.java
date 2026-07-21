package com.trading.crud.common;

/**
 * 【職責】資源不存在例外；由 Service 拋出後轉為 HTTP 404。
 * 【技巧】攜帶 {@link ErrorCodes}；{@link GlobalExceptionHandler} 統一產生 Problem Details。
 * 【概念】用 Optional／null 回傳也可以，但拋例外能讓 Controller 保持簡潔，並統一錯誤 JSON。
 * 【邊界】不負責查詢邏輯本身（由呼叫端決定何時拋出）。
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String errorCode;

    /**
     * @param errorCode 機器可讀錯誤碼（見 {@link ErrorCodes}）
     * @param message   人類可讀說明
     */
    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /** @return 錯誤碼字串 */
    public String getErrorCode() {
        return errorCode;
    }
}
