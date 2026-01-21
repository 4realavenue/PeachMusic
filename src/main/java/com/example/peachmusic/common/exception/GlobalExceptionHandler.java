package com.example.peachmusic.common.exception;

import com.example.peachmusic.common.model.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

        return ResponseEntity.status(ex.getStatusCode()).body(CommonResponse.fail(message));
    }

    // Custom 예외 - 공통 응답
    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<CommonResponse> handlerCustomException(CustomException ex) {

        log.error("CustomException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(CommonResponse.fail(ex.getErrorCode().getMessage()));
    }

    // 필수 파라미터가 누락되었을 때
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse> handleMissingParam(MissingServletRequestParameterException ex) {

        String param = switch (ex.getParameterName()) {
            case "word" -> "검색어";
            default -> ex.getParameterName();
        };
        String message = param + "를 입력해주세요.";

        log.error("MissingServletRequestParameterException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(CommonResponse.fail(message));
    }

}