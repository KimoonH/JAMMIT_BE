package com.jammit_be.common.exception;

import com.jammit_be.common.dto.CommonResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;

import static com.jammit_be.common.contant.JammitConstants.HttpConstant.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonResponse<?> defaultExceptionHandler(Exception e) {
        log.error("Exception", e);
        return new CommonResponse<>().fail(SERVER_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public CommonResponse<?> dataAccessExceptionHandler(DataAccessException e) {
        log.error("DataAccessException", e);
        return new CommonResponse<>().fail(SERVER_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(AlertException.class)
    public CommonResponse<?> alertExceptionHandler(AlertException e) {
        log.warn("AlertException", e);
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public CommonResponse<?> expiredJwtExceptionHandler(ExpiredJwtException e) {
        log.warn("ExpiredJwtException", e);
        return new CommonResponse<>().fail(UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException", e);
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).findFirst().orElse("유효성 검사 오류 발생");
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResponse<?> constraintViolationExceptionHandler(ConstraintViolationException e) {
        log.warn("ConstraintViolationException", e);
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResponse<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException", e);
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResponse<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.warn("lIllegalArgumentException", e);
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResponse<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException", e);
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public CommonResponse<?> bindExceptionHandler(BindException e) {
        return new CommonResponse<>().fail(CLIENT_FAIL_CODE, e.getMessage());
    }

}
