/**
 * 【職責】TradingCRUD 根套件：JWT 認證 + Order CRUD／批次的 Spring Boot 教學專案。
 * 【技巧】分層：auth／order（Controller+Service）、dto、domain、infrastructure、security、config、common。
 * 【概念】建議閱讀順序：AuthController → OrderController → OrderService → SecurityConfig → GlobalExceptionHandler。
 *         核心一句：JWT 保護 API；ADMIN 可寫、已登入可讀；clientOrderId 唯一性由 Service 統一驗證。
 */
package com.trading.crud;
