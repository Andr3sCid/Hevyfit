package com.hevyfit.routines.rest;

import com.hevyfit.common.error.ErrorCode;
import com.hevyfit.common.error.ErrorDetail;
import com.hevyfit.common.error.NotFoundException;
import com.hevyfit.common.error.ValidationException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/test/error-format")
public class ErrorFormatTestResource {

    @GET
    @Path("/validation")
    @Produces(MediaType.APPLICATION_JSON)
    public String validation() {
        throw new ValidationException("name is required",
                List.of(new ErrorDetail("name", ErrorCode.FIELD_REQUIRED)));
    }

    @GET
    @Path("/not-found")
    @Produces(MediaType.APPLICATION_JSON)
    public String notFound() {
        throw new NotFoundException("routine 42 not found");
    }
}
