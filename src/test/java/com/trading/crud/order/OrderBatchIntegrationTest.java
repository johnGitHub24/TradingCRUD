package com.trading.crud.order;

import com.trading.crud.order.infrastructure.OrderRepository;
import com.trading.crud.support.CrudTestFixtures;
import com.trading.crud.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】訂單批次 API 整合測試：全成功 201、部分失敗 207、驗證失敗 400、批次刪除。
 * 【技巧】MockMvc + ADMIN JWT；每測前清空 orders。
 * 【概念】保護「盡力而為」批次契約：部分失敗仍保留成功筆。
 */
class OrderBatchIntegrationTest extends IntegrationTestBase {

    @Autowired
    private OrderRepository orderRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        token = adminToken();
    }

    /**
     * CASE BATCH-001：三筆皆合法 → 201、succeeded=3。
     * Given: ADMIN + 合法批次 JSON；When: POST /batch；Then: 201 且 DB 有 3 筆。
     */
    @Test
    void BATCH_001_allValid_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("batch", "BATCH-001-SUCCESS")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requested").value(3))
                .andExpect(jsonPath("$.succeeded").value(3))
                .andExpect(jsonPath("$.failed").value(0));

        assertThat(orderRepository.count()).isEqualTo(3);
    }

    /**
     * CASE BATCH-006：批次內重複 clientOrderId → 207、部分成功。
     * Given: 含重複鍵的批次；When: POST /batch；Then: 207、succeeded=2、failed=1、DB=2。
     * 【技巧驗證】HTTP 207 Multi-Status 表達部分失敗。
     */
    @Test
    void BATCH_006_partialDuplicate_returns207MultiStatus() throws Exception {
        mockMvc.perform(post("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("batch", "BATCH-006-DUPLICATE")))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.requested").value(3))
                .andExpect(jsonPath("$.succeeded").value(2))
                .andExpect(jsonPath("$.failed").value(1));

        assertThat(orderRepository.count()).isEqualTo(2);
    }

    /**
     * CASE BATCH-003：空清單 → 400 VALIDATION_FAILED。
     * Given: orders 空；When: POST /batch；Then: 400。
     */
    @Test
    void BATCH_003_emptyList_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("batch", "BATCH-003-MISSING_REQUIRED")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    /**
     * CASE BATCH-007：刪除混合存在／不存在 ID → 207。
     * Given: 先建批再刪 [存在, 999999]；When: DELETE /batch；Then: 207、各 1 成敗。
     */
    @Test
    void BATCH_007_delete_mixedExisting_returns207() throws Exception {
        String created = mockMvc.perform(post("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("batch", "BATCH-001-SUCCESS")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long existingId = objectMapper.readTree(created).get("createdIds").get(0).asLong();
        String payload = "{\"ids\":[" + existingId + ",999999]}";

        mockMvc.perform(delete("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.requested").value(2))
                .andExpect(jsonPath("$.succeeded").value(1))
                .andExpect(jsonPath("$.failed").value(1));

        assertThat(orderRepository.count()).isEqualTo(2);
    }

    /**
     * CASE BATCH-008：刪除皆存在 → 200。
     * Given: 建 3 筆後刪其中 2；When: DELETE /batch；Then: 200、剩 1 筆。
     */
    @Test
    void BATCH_008_delete_allValid_returns200() throws Exception {
        String created = mockMvc.perform(post("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("batch", "BATCH-001-SUCCESS")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var ids = objectMapper.readTree(created).get("createdIds");
        String payload = "{\"ids\":[" + ids.get(0).asLong() + "," + ids.get(1).asLong() + "]}";

        mockMvc.perform(delete("/api/v1/orders/batch")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.succeeded").value(2))
                .andExpect(jsonPath("$.failed").value(0));

        assertThat(orderRepository.count()).isEqualTo(1);
    }
}
