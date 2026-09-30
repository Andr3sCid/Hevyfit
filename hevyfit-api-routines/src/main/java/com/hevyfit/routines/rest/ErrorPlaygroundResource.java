package com.hevyfit.routines.rest;

import com.hevyfit.common.error.ErrorCode;
import com.hevyfit.common.error.ErrorDetail;
import com.hevyfit.common.error.ForbiddenException;
import com.hevyfit.common.error.NotFoundException;
import com.hevyfit.common.error.UnauthorizedException;
import com.hevyfit.common.error.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// Endpoint manual para probar el formato de cada error de H0.1; borrar cuando ya no haga falta.
@Path("/test")
public class ErrorPlaygroundResource {

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

    @GET
    @Path("/unauthorized")
    @Produces(MediaType.APPLICATION_JSON)
    public String unauthorized() {
        throw new UnauthorizedException("missing or invalid token");
    }

    @GET
    @Path("/forbidden")
    @Produces(MediaType.APPLICATION_JSON)
    public String forbidden() {
        throw new ForbiddenException("user lacks the required role");
    }

    @GET
    @Path("/internal")
    @Produces(MediaType.APPLICATION_JSON)
    public String internal() {
        throw new RuntimeException("simulated unexpected failure");
    }

    @GET
    @Path("/constraint-violation")
    @Produces(MediaType.APPLICATION_JSON)
    public String constraintViolation(@QueryParam("name") @NotBlank String name) {
        return "name=" + name;
    }
}
