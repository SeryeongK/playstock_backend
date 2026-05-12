package com.playstock.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private final T data;
    private final ErrorDetail error;

    private ApiResponse(T data, ErrorDetail error) {
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(null, new ErrorDetail(code, message));
    }

    public T getData() {
        return data;
    }

    public ErrorDetail getError() {
        return error;
    }

    public record ErrorDetail(String code, String message) {}
}
