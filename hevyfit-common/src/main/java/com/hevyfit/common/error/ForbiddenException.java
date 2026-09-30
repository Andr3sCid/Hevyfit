package com.hevyfit.common.error;

public class ForbiddenException extends HevyFitException {

    public ForbiddenException(String message) {
        super(message);
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.FORBIDDEN;
    }

    @Override
    public int httpStatus() {
        return 403;
    }
}
