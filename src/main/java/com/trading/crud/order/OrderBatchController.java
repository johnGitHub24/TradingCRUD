package com.trading.crud.order;

import com.trading.crud.order.dto.BatchCreateRequest;
import com.trading.crud.order.dto.BatchDeleteRequest;
import com.trading.crud.order.dto.BatchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 【職責】訂單批次操作 API：接收批次請求、轉交 Service、依成功／失敗筆數決定 HTTP 狀態碼。
 * 【技巧】類別級 {@code @PreAuthorize("hasRole('ADMIN')")}；部分失敗回 {@link HttpStatus#MULTI_STATUS}（207）。
 * 【概念】207 Multi-Status 表示「部分成功、部分失敗」；body 的 {@link BatchResult} 列出每筆失敗原因。
 * 【邊界】不負責逐筆驗證與部分失敗統計（由 Service 處理）。
 */
@Tag(name = "Orders Batch", description = "訂單批次建立／刪除（僅 ADMIN）")
@RestController
@RequestMapping("/api/v1/orders/batch")
@PreAuthorize("hasRole('ADMIN')")
public class OrderBatchController {

    private final OrderService orderService;

    /** 建構子注入 Service，保持 Controller 無狀態、可測試。 */
    public OrderBatchController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 【職責】批次建立訂單（最多 500 筆）。
     * 【技巧】全部成功 → 201；有失敗 → 207 + {@link BatchResult}。
     * 【概念】與單筆 409「整筆失敗」不同：批次採盡力而為，成功筆仍會寫入。
     *
     * @param request 含 orders 清單
     * @return 批次結果
     */
    @Operation(summary = "批次建立訂單", description = "部分失敗時回傳 207 Multi-Status", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<BatchResult> batchCreate(@RequestBody @Valid BatchCreateRequest request) {
        BatchResult result = orderService.batchCreate(request);
        // 全部成功用 201；有任何失敗則用 207，body 仍含成功與失敗明細
        HttpStatus status = result.getFailed() == 0 ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * 【職責】批次刪除訂單（最多 500 筆）。
     * 【技巧】全部成功 → 200；部分失敗 → 207。
     * 【概念】找不到的 ID 記在 failures，不讓整批 rollback，方便前端逐筆處理。
     *
     * @param request 含 ids 清單
     * @return 批次結果
     */
    @Operation(summary = "批次刪除訂單", description = "部分失敗時回傳 207 Multi-Status", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping
    public ResponseEntity<BatchResult> batchDelete(@RequestBody @Valid BatchDeleteRequest request) {
        BatchResult result = orderService.batchDelete(request);
        HttpStatus status = result.getFailed() == 0 ? HttpStatus.OK : HttpStatus.MULTI_STATUS;
        return ResponseEntity.status(status).body(result);
    }
}
