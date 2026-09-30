package com.hevyfit.common.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JaxRsExceptionMapperTest {

    @Test
    void mapsKnownStatusesToTheirErrorCode() {
        assertEquals(ErrorCode.UNAUTHORIZED, JaxRsExceptionMapper.errorCodeFor(401));
        assertEquals(ErrorCode.FORBIDDEN, JaxRsExceptionMapper.errorCodeFor(403));
        assertEquals(ErrorCode.NOT_FOUND, JaxRsExceptionMapper.errorCodeFor(404));
    }

    @Test
    void mapsOtherClientErrorsToValidationError() {
        assertEquals(ErrorCode.VALIDATION_ERROR, JaxRsExceptionMapper.errorCodeFor(400));
        assertEquals(ErrorCode.VALIDATION_ERROR, JaxRsExceptionMapper.errorCodeFor(405));
    }

    @Test
    void mapsServerErrorsToInternalError() {
        assertEquals(ErrorCode.INTERNAL_ERROR, JaxRsExceptionMapper.errorCodeFor(500));
        assertEquals(ErrorCode.INTERNAL_ERROR, JaxRsExceptionMapper.errorCodeFor(503));
    }

    @Test
    void buildsErrorResponseWithResolvedMessage() {
        ErrorResponse response = JaxRsExceptionMapper.toErrorResponse(404, "es");

        assertEquals(ErrorCode.NOT_FOUND, response.code());
        assertEquals("Recurso no encontrado", response.message());
        assertEquals(0, response.details().size());
    }
}
