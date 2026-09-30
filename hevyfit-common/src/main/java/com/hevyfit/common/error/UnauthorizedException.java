package com.hevyfit.common.error;

public class UnauthorizedException extends HevyFitException {

    public UnauthorizedException(String message) {
        super(message);
    }

    @Override
    public ErrorCode errorCode() {
        return ErrorCode.UNAUTHORIZED;
    }

    @Override
    public int httpStatus() {
        return 401;
    }
}
