/**
 * 【職責】認證模組：登入換 JWT、查詢目前使用者。
 * 【技巧】薄 Controller + AuthService；JWT 簽發委託 security 套件。
 * 【概念】不直接操作 UserEntity、不自行解析 JWT。
 */
package com.trading.crud.auth;
