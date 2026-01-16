package com.example.peachmusic.common.exception;

import com.example.peachmusic.common.model.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();

        log.error("MethodArgumentNotValidException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(new CommonResponse(false, message, null));
    }

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<CommonResponse> handlerCustomException(CustomException ex) {

        log.error("CustomException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(new CommonResponse<>(false, ex.getErrorCode().getMessage(), null));
    }
}
