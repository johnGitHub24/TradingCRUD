package com.trading.crud.order.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 【職責】批次操作結果彙總：成功／失敗筆數與明細。
 * 【技巧】{@link #addSuccess}／{@link #addFailure} 累加計數；內嵌 {@link Item} 描述單筆失敗。
 * 【概念】採「盡力而為」——單筆失敗不 rollback 整批，呼叫端依 succeeded／failed 決定後續。
 * 【邊界】不含 HTTP 狀態碼選擇（由 Controller 依 failed 是否為 0 決定 201／200／207）。
 */
public class BatchResult {

    /** 請求中的總筆數 */
    private int requested;
    /** 成功處理的筆數 */
    private int succeeded;
    /** 失敗的筆數 */
    private int failed;
    /** 成功建立或刪除的訂單 ID 清單 */
    private List<Long> createdIds = new ArrayList<>();
    /** 失敗明細（索引、參考鍵、原因） */
    private List<Item> failures = new ArrayList<>();

    /**
     * 【職責】記錄一筆成功。
     * 【技巧】succeeded++，並把 id 加入 createdIds（刪除場景亦複用此清單）。
     * 【概念】統一成功路徑，方便 Controller 只看計數決定 HTTP 狀態。
     *
     * @param id 成功建立的訂單 ID，或刪除成功的 ID
     */
    public void addSuccess(Long id) {
        this.succeeded++;
        if (id != null) {
            this.createdIds.add(id);
        }
    }

    /**
     * 【職責】記錄一筆失敗。
     * 【技巧】failed++ 並新增 {@link Item}（index／reference／reason）。
     * 【概念】保留請求清單索引，前端可對應原資料列顯示錯誤。
     *
     * @param index     請求清單中的索引（0-based）
     * @param reference clientOrderId 或 id 字串
     * @param reason    失敗原因說明
     */
    public void addFailure(int index, String reference, String reason) {
        this.failed++;
        this.failures.add(new Item(index, reference, reason));
    }

    public int getRequested() {
        return requested;
    }

    public void setRequested(int requested) {
        this.requested = requested;
    }

    public int getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<Long> getCreatedIds() {
        return createdIds;
    }

    public void setCreatedIds(List<Long> createdIds) {
        this.createdIds = createdIds;
    }

    public List<Item> getFailures() {
        return failures;
    }

    public void setFailures(List<Item> failures) {
        this.failures = failures;
    }

    /**
     * 【職責】單筆批次失敗明細。
     * 【技巧】靜態內部類，隨 BatchResult 一併序列化為 JSON。
     * 【概念】index 對應請求陣列位置；reference 方便除錯（clientOrderId 或 id）。
     */
    public static class Item {
        /** 請求清單中的索引（0-based），方便前端對應原資料列 */
        private int index;
        /** 參考鍵：建立時為 clientOrderId，刪除時為 id 字串 */
        private String reference;
        /** 人類可讀的失敗原因 */
        private String reason;

        public Item() {
        }

        /**
         * @param index     請求清單索引
         * @param reference 參考鍵（clientOrderId 或 id）
         * @param reason    失敗原因
         */
        public Item(int index, String reference, String reason) {
            this.index = index;
            this.reference = reference;
            this.reason = reason;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
