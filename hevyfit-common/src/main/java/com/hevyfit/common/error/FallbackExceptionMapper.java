package com.hevyfit.common.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class FallbackExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(FallbackExceptionMapper.class.getName());

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Unhandled exception", exception);
        ErrorResponse body = toErrorResponse(exception, headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    static ErrorResponse toErrorResponse(Throwable exception, String acceptLanguage) {
        String message = ErrorMessages.resolve(ErrorCode.INTERNAL_ERROR, acceptLanguage);
        return new ErrorResponse(ErrorCode.INTERNAL_ERROR, message, List.of());
    }
}
