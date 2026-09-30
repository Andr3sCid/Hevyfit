package com.hevyfit.common.error;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class JaxRsExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        ErrorResponse body = toErrorResponse(status, headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    static ErrorResponse toErrorResponse(int status, String acceptLanguage) {
        ErrorCode code = errorCodeFor(status);
        String message = ErrorMessages.resolve(code, acceptLanguage);
        return new ErrorResponse(code, message, List.of());
    }

    static ErrorCode errorCodeFor(int status) {
        if (status == 401) {
            return ErrorCode.UNAUTHORIZED;
        }
        if (status == 403) {
            return ErrorCode.FORBIDDEN;
        }
        if (status == 404) {
            return ErrorCode.NOT_FOUND;
        }
        if (status >= 500) {
            return ErrorCode.INTERNAL_ERROR;
        }
        return ErrorCode.VALIDATION_ERROR;
    }
}
