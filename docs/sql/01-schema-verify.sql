-- ============================================================
-- TradingCRUD 資料庫結構驗證腳本
-- 適用：H2 Console / PostgreSQL
-- 執行時機：修改 Entity 或 Repository 後
-- ============================================================

-- 1. 確認資料表存在
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('ORDERS', 'APP_USERS', 'orders', 'app_users');

-- 2. orders 欄位結構
SHOW COLUMNS FROM orders;
-- PostgreSQL 替代：
-- SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'orders';

-- 3. app_users 欄位結構
SHOW COLUMNS FROM app_users;

-- 4. 唯一索引驗證（client_order_id 冪等）
SELECT INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES
WHERE TABLE_NAME = 'ORDERS' OR TABLE_NAME = 'orders';

-- 5. 關聯完整性（orders 無外鍵到其他表，但可確認無孤立資料）
SELECT COUNT(*) AS order_count FROM orders;
SELECT COUNT(*) AS user_count FROM app_users;

-- 6. 狀態分布統計
SELECT status, COUNT(*) AS cnt FROM orders GROUP BY status;

-- 7. 重複 client_order_id 檢查（應為 0）
SELECT client_order_id, COUNT(*) AS dup
FROM orders
GROUP BY client_order_id
HAVING COUNT(*) > 1;
