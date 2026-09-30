package com.hevyfit.common.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintViolationExceptionMapperTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void mapsNotNullViolationToFieldRequired() {
        List<ErrorDetail> details = detailsFor(new TestBean(null, "abc", 5));

        assertContains(details, "name", ErrorCode.FIELD_REQUIRED);
    }

    @Test
    void mapsSizeViolationBelowMinimumToFieldTooShort() {
        List<ErrorDetail> details = detailsFor(new TestBean("ok", "a", 5));

        assertContains(details, "code", ErrorCode.FIELD_TOO_SHORT);
    }

    @Test
    void mapsSizeViolationAboveMaximumToFieldTooLong() {
        List<ErrorDetail> details = detailsFor(new TestBean("ok", "abcdefghijk", 5));

        assertContains(details, "code", ErrorCode.FIELD_TOO_LONG);
    }

    @Test
    void mapsMinViolationToFieldOutOfRange() {
        List<ErrorDetail> details = detailsFor(new TestBean("ok", "abc", 0));

        assertContains(details, "amount", ErrorCode.FIELD_OUT_OF_RANGE);
    }

    private static List<ErrorDetail> detailsFor(TestBean bean) {
        Set<ConstraintViolation<TestBean>> violations = VALIDATOR.validate(bean);
        ConstraintViolationException exception = new ConstraintViolationException(violations);
        return ConstraintViolationExceptionMapper.toErrorResponse(exception, "es").details();
    }

    private static void assertContains(List<ErrorDetail> details, String field, ErrorCode code) {
        assertTrue(details.stream().anyMatch(d -> d.field().equals(field) && d.code() == code),
                () -> "expected " + field + "/" + code + " in " + details);
    }

    private static final class TestBean {
        @NotNull
        private final String name;

        @Size(min = 2, max = 10)
        private final String code;

        @Min(1)
        private final int amount;

        private TestBean(String name, String code, int amount) {
            this.name = name;
            this.code = code;
            this.amount = amount;
        }
    }
}
