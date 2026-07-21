package com.trading.crud.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 【職責】測試用 JSON fixture 載入器：從 {@code docs/test-data/{category}/{caseId}.json} 讀取。
 * 【技巧】{@link Files#readString}；失敗包成 {@link UncheckedIOException}。
 * 【概念】把案例資料與測試程式分離，同一份 JSON 可對照規格書 Case ID。
 * 【邊界】不負責解析／斷言，只回傳字串內容。
 */
public final class CrudTestFixtures {

    private CrudTestFixtures() {
    }

    /**
     * 【職責】載入指定分類與 Case ID 的 JSON 字串。
     * 【技巧】路徑相對專案根目錄 {@code docs/test-data/...}。
     * 【概念】Case ID 與檔名對齊（如 ORDER-001-SUCCESS），方便追蹤規格。
     *
     * @param category 子目錄（auth／order／batch）
     * @param caseId   不含副檔名的案例檔名
     * @return JSON 字串
     */
    public static String loadJson(String category, String caseId) {
        Path path = Paths.get("docs", "test-data", category, caseId + ".json");
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load fixture: " + category + "/" + caseId, e);
        }
    }
}
