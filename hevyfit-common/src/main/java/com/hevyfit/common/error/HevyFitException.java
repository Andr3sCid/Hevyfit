package com.hevyfit.common.error;

import java.util.List;

public abstract class HevyFitException extends RuntimeException {

    private final List<ErrorDetail> details;

    protected HevyFitException(String message) {
        this(message, List.of());
    }

    protected HevyFitException(String message, List<ErrorDetail> details) {
        super(message);
        this.details = details;
    }

    public abstract ErrorCode errorCode();

    public abstract int httpStatus();

    public List<ErrorDetail> details() {
        return details;
    }
}
