package com.example.notes.application.coolproject.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String message, String code, LocalDateTime timeStamp, int status) {}
