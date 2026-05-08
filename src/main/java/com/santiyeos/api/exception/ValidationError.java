package com.santiyeos.api.exception;

public record ValidationError(
        String field,
        String message
) {
}
