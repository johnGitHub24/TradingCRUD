/**
 * 【職責】訂單模組：單筆 CRUD 與批次建立／刪除。
 * 【技巧】Controller 轉交 OrderService；Mapper 做 Entity↔DTO；商業規則（唯一性、分頁上限）在 Service。
 * 【概念】Controller 不直接碰 Repository；Mapper 不寫查詢。
 */
package com.trading.crud.order;
