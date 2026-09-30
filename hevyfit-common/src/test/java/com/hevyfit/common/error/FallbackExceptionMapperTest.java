package com.hevyfit.common.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FallbackExceptionMapperTest {

    @Test
    void neverExposesTheRealExceptionMessage() {
        RuntimeException sensitive = new RuntimeException("connection string: postgresql://hevyfit:s3cr3t@localhost/db");

        ErrorResponse response = FallbackExceptionMapper.toErrorResponse(sensitive, "es");

        assertEquals(ErrorCode.INTERNAL_ERROR, response.code());
        assertEquals("Error interno del servidor", response.message());
        assertNotEquals(sensitive.getMessage(), response.message());
    }

    @Test
    void producesTheSameGenericMessageRegardlessOfTheException() {
        ErrorResponse first = FallbackExceptionMapper.toErrorResponse(new RuntimeException("a"), "es");
        ErrorResponse second = FallbackExceptionMapper.toErrorResponse(new IllegalStateException("b"), "es");

        assertEquals(first.message(), second.message());
    }
}
