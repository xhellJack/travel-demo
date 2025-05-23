package com.example.travel.exception;

import lombok.Getter;

/**
 * 基础业务异常（所有自定义异常的父类）
 */
@Getter
public class BaseException extends RuntimeException {
    private final int code; // HTTP状态码
    private final String detail; // 调试详情（可选）

    public BaseException(int code, String message) {
        this(code, message, null);
    }

    public BaseException(int code, String message, String detail) {
        super(message);
        this.code = code;
        this.detail = detail;
    }
}