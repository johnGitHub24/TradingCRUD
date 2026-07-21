# 參考資料 — houseHub 測試方法論（TradingCRUD 採用版）

> 本文件僅供參考。正式開發以 [`TradingCRUD 規格書.md`](TradingCRUD%20規格書.md) 為準。

---

## 採用項目

| 方法論 | TradingCRUD 實作 |
|--------|------------------|
| fixture 分層 | `docs/test-data/{auth,order,batch}/` |
| Case ID 命名 | `AUTH-001-SUCCESS`、`ORDER-006-DUPLICATE` |
| 三層測試 | unit / integration / smoke(verify-db.ps1) |
| 每 API 最低案例 | 001-SUCCESS、003-MISSING、006-DUPLICATE |
| DoD 清單 | docs/測試規格書.md §6 |

## 不採用項目

- Grails Controller / Spock
- houseHub API 路由
- `000000` 錯誤碼格式

---

*最後更新：2026-07-09*
