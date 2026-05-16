package com.playstock.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "잘못된 입력입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다"),

    // User
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다"),

    // Channel
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"),

    // Trading
    INSUFFICIENT_SHARES(HttpStatus.BAD_REQUEST, "INSUFFICIENT_SHARES", "잔여 조각이 부족합니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
