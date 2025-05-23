package com.example.travel.exception;

public class BusinessValidationException extends BaseException {
    public BusinessValidationException(String message) {
        super(400, message);
    }
    public BusinessValidationException(String message, String detail) {
        super(400, message, detail);
    }
}