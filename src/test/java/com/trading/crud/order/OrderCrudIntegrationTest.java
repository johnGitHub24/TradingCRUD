package com.trading.crud.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.trading.crud.order.infrastructure.OrderRepository;
import com.trading.crud.support.CrudTestFixtures;
import com.trading.crud.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】訂單單筆 CRUD 整合測試：建立／查詢／驗證／更新／重複／刪除／分頁。
 * 【技巧】MockMvc + ADMIN JWT + fixture JSON；每測前清空 orders。
 * 【概念】端到端驗證 HTTP 契約與 DB 副作用（count、404）。
 */
class OrderCrudIntegrationTest extends IntegrationTestBase {

    @Autowired
    private OrderRepository orderRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository.deleteAll();
        token = adminToken();
    }

    private long createOrder(String caseId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("order", caseId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    /**
     * CASE ORDER-001：建立成功回 201、status=NEW。
     * Given: 合法 body；When: POST /orders；Then: 201 + DB 1 筆。
     */
    @Test
    void ORDER_001_create_returns201WithLocation() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("order", "ORDER-001-SUCCESS")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clientOrderId").value("crud-order-001"))
                .andExpect(jsonPath("$.status").value("NEW"));

        assertThat(orderRepository.count()).isEqualTo(1);
    }

    /**
     * CASE ORDER-002：依 id 查詢回 200。
     * Given: 已建立訂單；When: GET /orders/{id}；Then: 200 + symbol。
     */
    @Test
    void ORDER_002_getById_returns200() throws Exception {
        long id = createOrder("ORDER-001-SUCCESS");

        mockMvc.perform(get("/api/v1/orders/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.symbol").value("BTCUSDT"));
    }

    /**
     * CASE ORDER-003：缺必填回 400，DB 不變。
     * Given: 缺欄位 body；When: POST；Then: 400、count=0。
     */
    @Test
    void ORDER_003_missingRequired_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("order", "ORDER-003-MISSING_REQUIRED")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));

        assertThat(orderRepository.count()).isZero();
    }

    /**
     * CASE ORDER-004：格式非法回 400。
     * Given: 非法格式 body；When: POST；Then: 400 VALIDATION_FAILED。
     */
    @Test
    void ORDER_004_invalidFormat_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("order", "ORDER-004-INVALID_FORMAT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    /**
     * CASE ORDER-005：更新成功回 200。
     * Given: 已有訂單；When: PUT；Then: symbol/side/status 已變更。
     */
    @Test
    void ORDER_005_update_returns200() throws Exception {
        long id = createOrder("ORDER-001-SUCCESS");

        mockMvc.perform(put("/api/v1/orders/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("order", "ORDER-UPDATE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("ETHUSDT"))
                .andExpect(jsonPath("$.side").value("SELL"))
                .andExpect(jsonPath("$.status").value("FILLED"));
    }

    /**
     * CASE ORDER-006：重複 clientOrderId 回 409。
     * Given: 同 clientOrderId 已存在；When: 再 POST；Then: 409 DUPLICATE_ORDER、仍 1 筆。
     */
    @Test
    void ORDER_006_duplicateClientOrderId_returns409() throws Exception {
        createOrder("ORDER-001-SUCCESS");

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CrudTestFixtures.loadJson("order", "ORDER-001-SUCCESS")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_ORDER"));

        assertThat(orderRepository.count()).isEqualTo(1);
    }

    /**
     * CASE ORDER-007：刪除回 204，再查 404。
     * Given: 已有訂單；When: DELETE 再 GET；Then: 204 後 ORDER_NOT_FOUND。
     */
    @Test
    void ORDER_007_delete_returns204ThenNotFound() throws Exception {
        long id = createOrder("ORDER-001-SUCCESS");

        mockMvc.perform(delete("/api/v1/orders/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/orders/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
    }

    /**
     * CASE ORDER-008：列表回分頁 meta。
     * Given: 1 筆訂單；When: GET ?page=0&size=10；Then: data 陣列 + meta.total=1。
     */
    @Test
    void ORDER_008_list_returnsPagedMeta() throws Exception {
        createOrder("ORDER-001-SUCCESS");

        String body = mockMvc.perform(get("/api/v1/orders?page=0&size=10")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(10))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(body);
        assertThat(node.get("meta").get("total").asLong()).isEqualTo(1);
    }
}
