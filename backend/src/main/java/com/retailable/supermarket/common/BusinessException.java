package com.retailable.supermarket.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final int code;
    private final HttpStatus status;

    public BusinessException(int code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public int getCode() { return code; }
    public HttpStatus getStatus() { return status; }

    public static BusinessException badRequest(String message) {
        return new BusinessException(4000, HttpStatus.BAD_REQUEST, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(4010, HttpStatus.UNAUTHORIZED, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(4030, HttpStatus.FORBIDDEN, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(4040, HttpStatus.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(4090, HttpStatus.CONFLICT, message);
    }
}

