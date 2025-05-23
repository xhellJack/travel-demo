package com.example.travel.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
    public UnauthorizedException(String message, String detail) {
        super(401, message, detail);
    }
}