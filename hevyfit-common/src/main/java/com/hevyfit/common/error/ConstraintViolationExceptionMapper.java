package com.hevyfit.common.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ErrorResponse body = toErrorResponse(exception, headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE));
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    static ErrorResponse toErrorResponse(ConstraintViolationException exception, String acceptLanguage) {
        String message = ErrorMessages.resolve(ErrorCode.VALIDATION_ERROR, acceptLanguage);
        return new ErrorResponse(ErrorCode.VALIDATION_ERROR, message, toDetails(exception.getConstraintViolations()));
    }

    private static List<ErrorDetail> toDetails(Set<ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> new ErrorDetail(fieldName(violation), fieldErrorCode(violation)))
                .collect(Collectors.toList());
    }

    private static String fieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }

    private static ErrorCode fieldErrorCode(ConstraintViolation<?> violation) {
        Class<? extends Annotation> type = violation.getConstraintDescriptor().getAnnotation().annotationType();
        if (type == NotNull.class || type == NotBlank.class || type == NotEmpty.class) {
            return ErrorCode.FIELD_REQUIRED;
        }
        if (type == Size.class) {
            return sizeErrorCode(violation);
        }
        if (isRangeConstraint(type)) {
            return ErrorCode.FIELD_OUT_OF_RANGE;
        }
        return ErrorCode.FIELD_INVALID;
    }

    private static boolean isRangeConstraint(Class<? extends Annotation> type) {
        return type == Min.class || type == Max.class
                || type == DecimalMin.class || type == DecimalMax.class
                || type == Positive.class || type == PositiveOrZero.class
                || type == Negative.class || type == NegativeOrZero.class;
    }

    private static ErrorCode sizeErrorCode(ConstraintViolation<?> violation) {
        Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();
        int min = (int) attributes.get("min");
        int length = actualLength(violation.getInvalidValue());
        return length < min ? ErrorCode.FIELD_TOO_SHORT : ErrorCode.FIELD_TOO_LONG;
    }

    private static int actualLength(Object invalidValue) {
        if (invalidValue instanceof CharSequence sequence) {
            return sequence.length();
        }
        if (invalidValue instanceof Collection<?> collection) {
            return collection.size();
        }
        if (invalidValue instanceof Map<?, ?> map) {
            return map.size();
        }
        if (invalidValue != null && invalidValue.getClass().isArray()) {
            return Array.getLength(invalidValue);
        }
        return 0;
    }
}
