package com.example.travel.exception;

public class ExceptionUtils {

    // 快速抛出资源不存在异常
    public static void throwIfNotFound(boolean condition, String message) {
        if (!condition) {
            throw new ResourceNotFoundException(message);
        }
    }

    // 快速抛出业务校验异常
    public static void throwIfInvalid(boolean condition, String message) {
        if (!condition) {
            throw new BusinessValidationException(message);
        }
    }
}