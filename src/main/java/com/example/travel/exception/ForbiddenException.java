package com.example.travel.exception;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(403, message);
    }
    public ForbiddenException(String message, String detail) {
        super(403, message, detail);
    }
}