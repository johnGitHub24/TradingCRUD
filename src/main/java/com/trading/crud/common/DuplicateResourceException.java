package com.trading.crud.common;

/**
 * 【職責】資源重複（衝突）例外，例如 clientOrderId 已存在。
 * 【技巧】攜帶 {@link ErrorCodes} 機器可讀碼；由 {@link GlobalExceptionHandler} 轉為 HTTP 409。
 * 【概念】Service 拋「領域例外」而非直接回 HTTP，讓同一規則可被 REST、批次、未來排程共用。
 * 【邊界】不負責組裝 Problem Details JSON（由全域處理器負責）。
 */
public class DuplicateResourceException extends RuntimeException {

    private final String errorCode;

    /**
     * @param errorCode 機器可讀錯誤碼（見 {@link ErrorCodes}）
     * @param message   人類可讀說明
     */
    public DuplicateResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /** @return 錯誤碼字串 */
    public String getErrorCode() {
        return errorCode;
    }
}
