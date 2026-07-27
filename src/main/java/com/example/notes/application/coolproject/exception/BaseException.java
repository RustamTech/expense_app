package com.example.notes.application.coolproject.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
abstract public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    protected  BaseException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

}
