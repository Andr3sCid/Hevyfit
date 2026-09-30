package com.hevyfit.common.error;

public class NotFoundException extends HevyFitException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.NOT_FOUND;
    }

    @Override
    public int httpStatus() {
        return 404;
    }
}
