package com.trading.crud.order;

import com.trading.crud.order.domain.OrderStatus;
import com.trading.crud.order.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * 【職責】訂單 REST API 入口：收參數（含 {@code @Valid}）、轉交 {@link OrderService}、組 HTTP 回應。
 * 【技巧】薄 Controller；{@code @PreAuthorize("hasRole('ADMIN')")} 保護寫入；基底 {@code /api/v1/orders}。
 * 【概念】{@code @Valid} 失敗由全域處理器回 400；角色檢查在方法執行「之前」完成。
 * 【邊界】不負責 clientOrderId 唯一性、Entity 轉換、分頁計算（皆在 Service）。
 */
@Tag(name = "Orders", description = "訂單 CRUD（ADMIN 可寫入，已登入可讀取）")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /** 建構子注入 Service，保持 Controller 無狀態、可測試。 */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 【職責】建立單筆訂單；成功回 201 + Location。
     * 【技巧】{@code ResponseEntity.created(location)}；Location 用 {@link ServletUriComponentsBuilder} 組裝。
     * 【概念】REST 慣例：新建資源應回 201 並指出新資源 URL。重複 clientOrderId → Service 拋衝突 → 409。
     *
     * @param request 建立訂單請求
     * @return 已建立訂單與 Location header
     */
    @Operation(summary = "建立訂單", description = "需 ADMIN 角色；clientOrderId 必須唯一", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request) {
        OrderResponse response = orderService.create(request);
        // REST 慣例：201 Created 應附 Location header，指向新資源的 URL
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 【職責】依 ID 查詢單筆訂單。
     * 【技巧】{@code @PathVariable}；不存在時由全域例外處理回 404。
     * 【概念】讀取端點不需 ADMIN，已登入即可（SecurityConfig 的 authenticated）。
     *
     * @param id 訂單主鍵
     * @return 訂單詳情
     */
    @Operation(summary = "查詢單筆訂單", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public OrderResponse getOne(@PathVariable Long id) {
        return orderService.getById(id);
    }

    /**
     * 【職責】分頁列出訂單（可選 symbol、status 篩選）。
     * 【技巧】{@code @RequestParam} 可選參數；預設 page=0、size=20。
     * 【概念】分頁避免一次回傳全表；size 上限由 Service 再夾緊到 100。
     *
     * @param symbol 商品代碼（可選）
     * @param status 訂單狀態（可選）
     * @param page   頁碼，預設 0
     * @param size   每頁筆數，預設 20
     * @return 分頁資料與 meta
     */
    @Operation(summary = "分頁列出訂單", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public PagedResponse<OrderResponse> list(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.list(symbol, status, page, size);
    }

    /**
     * 【職責】更新訂單（含狀態）。
     * 【技巧】PUT + {@code @PreAuthorize}；body 經 {@code @Valid}。
     * 【概念】本教學允許直接改 status；真實撮合系統通常由引擎驅動狀態，而非客戶端任意 PUT。
     *
     * @param id      訂單主鍵
     * @param request 更新欄位
     * @return 更新後訂單
     */
    @Operation(summary = "更新訂單", description = "需 ADMIN 角色", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse update(@PathVariable Long id, @RequestBody @Valid UpdateOrderRequest request) {
        return orderService.update(id, request);
    }

    /**
     * 【職責】刪除單筆訂單，回 204 No Content。
     * 【技巧】{@code ResponseEntity.noContent()}；刪除成功無 body。
     * 【概念】204 表示成功但無回傳內容；之後再 GET 同一 id 應為 404。
     *
     * @param id 訂單主鍵
     * @return HTTP 204
     */
    @Operation(summary = "刪除訂單", description = "需 ADMIN 角色", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
