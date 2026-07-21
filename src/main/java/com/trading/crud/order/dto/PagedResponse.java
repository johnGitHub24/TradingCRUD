package com.trading.crud.order.dto;

import java.util.List;

/**
 * 【職責】通用分頁回應包裝：目前頁資料 + 分頁中繼資料。
 * 【技巧】泛型 {@code <T>}；內嵌 {@link PageMeta}；工廠方法 {@link PageMeta#of}。
 * 【概念】data 與 meta 分開，前端可用 meta.total 算總頁數，不必另開 count API。
 *
 * @param <T> 資料列類型（例如 {@link OrderResponse}）
 */
public class PagedResponse<T> {

    /** 目前頁的資料列 */
    private List<T> data;
    /** 分頁中繼資料（頁碼、每頁筆數、總筆數） */
    private PageMeta meta;

    /** 預設建構子（JSON 反序列化用）。 */
    public PagedResponse() {
    }

    /**
     * @param data 目前頁資料列
     * @param meta 分頁中繼資料
     */
    public PagedResponse(List<T> data, PageMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PageMeta getMeta() {
        return meta;
    }

    public void setMeta(PageMeta meta) {
        this.meta = meta;
    }

    /**
     * 【職責】分頁中繼資料（頁碼、每頁筆數、總筆數）。
     * 【技巧】靜態內部類；{@link #of} 工廠方法簡化建立。
     * 【概念】total 是符合篩選的總筆數，不是目前頁的 size。
     */
    public static class PageMeta {
        /** 目前頁碼，從 0 開始 */
        private int page;
        /** 每頁筆數 */
        private int size;
        /** 符合篩選條件的總筆數（非僅目前頁） */
        private long total;

        public PageMeta() {
        }

        public PageMeta(int page, int size, long total) {
            this.page = page;
            this.size = size;
            this.total = total;
        }

        /**
         * 工廠方法建立 PageMeta。
         *
         * @param page  目前頁碼
         * @param size  每頁筆數
         * @param total 符合條件的總筆數
         * @return PageMeta 實例
         */
        public static PageMeta of(int page, int size, long total) {
            return new PageMeta(page, size, total);
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }
}
