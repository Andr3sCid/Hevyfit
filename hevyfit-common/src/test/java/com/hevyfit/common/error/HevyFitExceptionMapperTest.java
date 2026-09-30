package com.hevyfit.common.error;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HevyFitExceptionMapperTest {

    @Test
    void mapsValidationExceptionWithDetails() {
        List<ErrorDetail> details = List.of(new ErrorDetail("height", ErrorCode.FIELD_REQUIRED));
        ValidationException exception = new ValidationException("height is required", details);

        ErrorResponse response = HevyFitExceptionMapper.toErrorResponse(exception, "es");

        assertEquals(ErrorCode.VALIDATION_ERROR, response.code());
        assertEquals("Datos inválidos", response.message());
        assertEquals(details, response.details());
    }

    @Test
    void mapsNotFoundExceptionWithoutDetails() {
        NotFoundException exception = new NotFoundException("measurement 42 not found");

        ErrorResponse response = HevyFitExceptionMapper.toErrorResponse(exception, "es");

        assertEquals(ErrorCode.NOT_FOUND, response.code());
        assertEquals("Recurso no encontrado", response.message());
        assertEquals(List.of(), response.details());
    }
}
