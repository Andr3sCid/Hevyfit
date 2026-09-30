package com.hevyfit.common.error;

import java.util.List;

public class ValidationException extends HevyFitException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<ErrorDetail> details) {
        super(message, details);
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.VALIDATION_ERROR;
    }

    @Override
    public int httpStatus() {
        return 400;
    }
}
