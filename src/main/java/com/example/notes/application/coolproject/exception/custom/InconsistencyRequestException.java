package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InconsistencyRequestException extends BaseException {

    public InconsistencyRequestException(String message){
        super(
                ErrorCode.INCONSISTENCY_REQUEST,
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
