package com.example.travel.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(409, message);
    }
    public ConflictException(String message, String detail) {
        super(409, message, detail);
    }
}