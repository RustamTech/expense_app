package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseException {

    public ResourceAlreadyExistsException(String message){
        super(
                ErrorCode.RESOURCE_ALREADY_EXISTS,
                HttpStatus.CONFLICT,
                message
        );
    }
}
