package com.example.peachmusic.common.exception;

import com.example.peachmusic.common.model.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Valid 예외 - 공통 응답
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();

        log.error("MethodArgumentNotValidException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(CommonResponse.fail(message));
    }

    // Custom 예외 - 공통 응답
    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<CommonResponse<Void>> handlerCustomException(CustomException ex) {

        log.error("CustomException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(CommonResponse.fail(ex.getErrorCode().getMessage()));
    }

    // 필수 파라미터가 누락되었을 때
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {

        String param = switch (ex.getParameterName()) {
            case "word" -> "검색어";
            default -> ex.getParameterName();
        };
        String message = param + "를 입력해주세요.";

        log.error("MissingServletRequestParameterException 발생 : {} ", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(CommonResponse.fail(message));
    }

    // 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

        log.error("MethodArgumentTypeMismatchException 발생 : {} ", ex.getMessage());

        String param = switch (ex.getName()) {
            case "artistId" -> "아티스트";
            case "albumId" -> "앨범";
            case "songId" -> "음원";
            case "playlistId" -> "플레이리스트";
            default -> ex.getName();
        };

        String message = param + " 요청 값이 올바르지 않습니다.";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.fail(message));
    }

    // Request Body를 읽을 수 없을 때(Json 형식 오류 / Body 내부 타입 불일치)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        log.error("HttpMessageNotReadableException 발생 : {}", ex.getMessage());

        String message = "입력하신 요청이 올바르지 않습니다.";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.fail(message));
    }

    // Request Method 불일치
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {

        log.error("HttpRequestMethodNotSupportedException 발생 : {}", ex.getMessage());

        String method = switch (ex.getMethod()) {
            case "POST" -> "입력";
            case "GET" -> "읽기";
            case "PUT", "PATCH" -> "수정";
            case "DELETE" -> "삭제";
            default -> "해당";
        };

        String message = method + " 요청은 지원하지 않는 요청입니다.";

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(CommonResponse.fail(message));
    }

    // 서버에 존재하지 않는 url로 접근 요청 시
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {

        log.error("NoHandlerFoundException 발생 : {}", ex.getMessage());

        String message = "페이지를 찾을 수 없습니다.";

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.fail(message));
    }

    // 예외처리 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception ex) {

        log.error("{} Exception 발생 : {}", ex, ex.getMessage());

        String message = "오류가 발생 했습니다.";

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponse.fail(message));
    }
}