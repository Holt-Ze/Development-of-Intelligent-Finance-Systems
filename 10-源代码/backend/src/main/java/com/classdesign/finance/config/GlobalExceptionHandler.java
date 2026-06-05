package com.classdesign.finance.config;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.common.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException exception) {
        return ApiResponse.failure(exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<?> handleValidationException(Exception exception) {
        return ApiResponse.failure("请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception exception) {
        return ApiResponse.failure(exception.getMessage() == null ? "系统异常" : exception.getMessage());
    }
}
