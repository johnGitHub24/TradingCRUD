/**
 * 【職責】共用例外、錯誤碼與全域 Problem Details 處理。
 * 【技巧】Service 拋語意化例外，由 GlobalExceptionHandler 統一組 HTTP body。
 * 【概念】Controller 不必各自 try-catch 組錯誤 JSON。
 */
package com.trading.crud.common;
