package com.example.peachmusic.common.model;

import com.example.peachmusic.common.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    public CommonResponse(boolean success, String message, T data) {

        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();

    }

    // 성공시 공용 응답 객체
    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, message, data);
    }

    public static <T> CommonResponse<T> fail(String message, T data) {
        return new CommonResponse<>(false, message, data);
    }

    public static CommonResponse<Void> fail(ErrorCode errorCode) {
        return new CommonResponse<>(false, errorCode.getMessage(), null);
    }
}
