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
    DUPLICATE_CHANNEL(HttpStatus.CONFLICT, "DUPLICATE_CHANNEL", "이미 등록된 채널입니다"),
    YOUTUBE_CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "YOUTUBE_CHANNEL_NOT_FOUND", "YouTube에서 채널을 찾을 수 없습니다"),

    // Trading
    INSUFFICIENT_SHARES(HttpStatus.BAD_REQUEST, "INSUFFICIENT_SHARES", "잔여 조각이 부족합니다"),

    // OAuth
    OAUTH_INVALID_STATE(HttpStatus.BAD_REQUEST, "OAUTH_INVALID_STATE", "유효하지 않은 OAuth state입니다"),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "OAUTH_TOKEN_EXCHANGE_FAILED", "Google 토큰 발급에 실패했습니다"),
    OAUTH_TOKEN_REFRESH_FAILED(HttpStatus.BAD_GATEWAY, "OAUTH_TOKEN_REFRESH_FAILED", "Google 토큰 갱신에 실패했습니다"),
    OAUTH_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "OAUTH_NOT_CONNECTED", "YouTube 채널이 연동되지 않았습니다"),
    YOUTUBE_MY_CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "YOUTUBE_MY_CHANNEL_NOT_FOUND", "연동된 YouTube 채널을 찾을 수 없습니다"),
    TOKEN_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_ENCRYPTION_FAILED", "토큰 처리 중 오류가 발생했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
