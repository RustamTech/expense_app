package com.example.notes.application.coolproject.exception.custom;

import com.example.notes.application.coolproject.exception.BaseException;
import com.example.notes.application.coolproject.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ExpenseNotFoundException extends BaseException {

    public ExpenseNotFoundException(Long id){
        super(
                ErrorCode.EXPENSE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Expense with id " + id + " not found"
        );
    }
}
