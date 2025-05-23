package com.example.travel.exception;

import com.example.travel.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器（返回统一JSON格式）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 记录异常日志的通用方法
    private void logError(HttpServletRequest request, Exception ex, String message) {
        log.error("Request URL: {}, Method: {}, Error: {}",
                request.getRequestURL(),
                request.getMethod(),
                message,
                ex);
    }
    // 自定义业务异常
    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("{} {} → code={}, message={}, detail={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getCode(),
                ex.getMessage(),
                ex.getDetail() != null ? ex.getDetail() : "none");
        return Result.error(ex.getCode(), ex.getMessage());
    }


    // 处理@RequestBody参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        logError(request, ex, "Validation error: " + message);
        return Result.error(400, message);
    }

    // 处理@RequestParam/@PathVariable参数校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        logError(request, ex, "Validation error: " + message);
        return Result.error(400, message);
    }

    // 处理表单提交校验异常
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(
            BindException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("表单校验失败");
        logError(request, ex, "Form validation error: " + message);
        return Result.error(400, message);
    }

    // 处理其他未捕获异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        logError(request, ex, "Internal server error");
        return Result.error(500, "服务器繁忙，请稍后重试");
    }
}