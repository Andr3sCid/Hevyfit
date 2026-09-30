package com.hevyfit.common.error;

import java.util.List;

public record ErrorResponse(ErrorCode code, String message, List<ErrorDetail> details) {

    public ErrorResponse(ErrorCode code, String message) {
        this(code, message, List.of());
    }
}
