package com.example.peachmusic.common.exception;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Valid 예외 - 공통 응답
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();

        log.error("MethodArgumentNotValidException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(new CommonResponse(false, message, null));
    }

    // Custom 예외 - 공통 응답
    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<CommonResponse> handlerCustomException(CustomException ex) {

        log.error("CustomException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(CommonResponse.fail(ex.getErrorCode()));
    }

    // Custom 예외 - 공통 페이지 응답
    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<PageResponse> handlerCustomPageException(CustomException ex) {

        log.error("CustomException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(PageResponse.fail(ex.getErrorCode()));

    }
}
