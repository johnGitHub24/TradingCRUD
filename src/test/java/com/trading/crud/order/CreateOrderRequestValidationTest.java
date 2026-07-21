package com.trading.crud.order;

import com.trading.crud.order.domain.OrderSide;
import com.trading.crud.order.dto.CreateOrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】單元測試 CreateOrderRequest 的 Bean Validation 約束。
 * 【技巧】純 Validator（無 Spring 容器）；{@code @Tag("unit")}。
 * 【概念】在進整合測試前先鎖住 DTO 驗證規則，失敗成本更低。
 */
@Tag("unit")
class CreateOrderRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private CreateOrderRequest valid() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setClientOrderId("crud-001");
        request.setSymbol("BTCUSDT");
        request.setSide(OrderSide.BUY);
        request.setQuantity(new BigDecimal("0.5"));
        request.setPrice(new BigDecimal("65000.00"));
        return request;
    }

    /**
     * CASE DTO-001：合法請求無違規。
     * Given: 完整合法欄位；When: validate；Then: violations 為空。
     */
    @Test
    void DTO_001_validRequest_hasNoViolations() {
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(valid());
        assertThat(violations).isEmpty();
    }

    /**
     * CASE DTO-002：缺 clientOrderId 有違規。
     * Given: clientOrderId=null；When: validate；Then: 違規欄位含 clientOrderId。
     */
    @Test
    void DTO_002_missingClientOrderId_hasViolation() {
        CreateOrderRequest request = valid();
        request.setClientOrderId(null);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("clientOrderId"));
    }

    /**
     * CASE DTO-003：quantity ≤ 0 有違規。
     * Given: quantity=-1；When: validate；Then: 違規欄位含 quantity。
     * 【技巧驗證】{@code @DecimalMin(inclusive=false)} 擋非正數。
     */
    @Test
    void DTO_003_nonPositiveQuantity_hasViolation() {
        CreateOrderRequest request = valid();
        request.setQuantity(new BigDecimal("-1"));

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }
}
