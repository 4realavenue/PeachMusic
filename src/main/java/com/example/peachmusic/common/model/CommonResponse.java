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


    // 성공 응답 (응답 데이터 없음)
    public static CommonResponse<Void> success(String message) {
        return new CommonResponse<>(true, message, null);
    }

    // 성공 응답 (응답 데이터 포함)
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data);
    }

    // 실패 응답 (응답 데이터 없음)
    public static CommonResponse<Void> fail(ErrorCode errorCode) {
        return new CommonResponse<>(false, errorCode.getMessage(), null);
    }
}