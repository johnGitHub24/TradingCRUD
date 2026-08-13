package com.trading.crud.auth;

import com.trading.crud.auth.dto.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】LoginRequest Bean Validation 單元測試，與 AUTH-003 整合層同一契約。
 * 【技巧】純 Validator（無 Spring）；{@code @Tag("unit")}。
 * 【概念】缺帳密在進 AuthenticationManager 前就應被 @Valid 擋下。
 */
@Tag("unit")
class LoginRequestValidationTest {

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

    /**
     * CASE AUTH-003：缺 username／password 有違規。
     * Given: 空欄位；When: validate；Then: 含 username 或 password 違規。
     */
    @Test
    void AUTH_003_missingRequired_hasViolations() {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword(null);

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
