-- ============================================================
-- TradingCRUD CRUD 操作驗證腳本（手動 SQL）
-- 說明：模擬 Service 層邏輯，確認 DB 層行為正確
-- ============================================================

-- 前置：確認 admin 已 seed
SELECT id, username, role, enabled FROM app_users WHERE username = 'admin';

-- 新增測試訂單
INSERT INTO orders (client_order_id, symbol, side, quantity, price, status, created_at, updated_at)
VALUES ('sql-test-001', 'ETHUSDT', 'BUY', 1.5, 3200.00, 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 查詢
SELECT id, client_order_id, symbol, side, quantity, price, status, created_at
FROM orders WHERE client_order_id = 'sql-test-001';

-- 更新
UPDATE orders SET status = 'FILLED', price = 3250.00, updated_at = CURRENT_TIMESTAMP
WHERE client_order_id = 'sql-test-001';

-- 驗證更新
SELECT status, price FROM orders WHERE client_order_id = 'sql-test-001';

-- 刪除
DELETE FROM orders WHERE client_order_id = 'sql-test-001';

-- 確認已刪除
SELECT COUNT(*) AS should_be_zero FROM orders WHERE client_order_id = 'sql-test-001';

-- 冪等測試：重複 INSERT 應失敗（UNIQUE 約束）
-- INSERT INTO orders (...) VALUES ('sql-test-dup', ...);
-- INSERT INTO orders (...) VALUES ('sql-test-dup', ...);  -- 預期失敗
