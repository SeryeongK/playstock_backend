package com.playstock.common.exception;

import lombok.Getter;

@Getter
public class PlaystockException extends RuntimeException {

    private final ErrorCode errorCode;

    public PlaystockException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
