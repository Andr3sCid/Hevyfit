package com.hevyfit.common.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class HevyFitExceptionMapper implements ExceptionMapper<HevyFitException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(HevyFitException exception) {
        ErrorResponse body = toErrorResponse(exception, headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
        return Response.status(exception.httpStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    static ErrorResponse toErrorResponse(HevyFitException exception, String acceptLanguage) {
        String message = ErrorMessages.resolve(exception.errorCode(), acceptLanguage);
        return new ErrorResponse(exception.errorCode(), message, exception.details());
    }
}
