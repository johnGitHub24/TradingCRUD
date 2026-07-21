package com.trading.crud.common;

/**
 * 【職責】API 錯誤碼常數，寫入 Problem Details 的 {@code errorCode} 欄位。
 * 【技巧】與 {@link GlobalExceptionHandler}、領域例外搭配；字串常數集中管理避免拼錯。
 * 【概念】HTTP status（404、409）告訴前端「錯誤類型」；errorCode 提供更細業務語意，
 *         方便顯示不同訊息或做國際化。
 * 【邊界】不含 HTTP 狀態碼本身（狀態碼由 Handler 決定）。
 */
public final class ErrorCodes {

    /** Bean Validation 失敗 */
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    /** 訂單不存在 */
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    /** clientOrderId 重複 */
    public static final String DUPLICATE_ORDER = "DUPLICATE_ORDER";
    /** 批次操作部分失敗 */
    public static final String BATCH_PARTIAL_FAILURE = "BATCH_PARTIAL_FAILURE";
    /** HTTP 方法不允許 */
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    /** 未認證 */
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    /** 權限不足 */
    public static final String FORBIDDEN = "FORBIDDEN";
    /** 帳密錯誤 */
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";
    /** 未預期伺服器錯誤 */
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCodes() {
    }
}
