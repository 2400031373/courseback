package com.example.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Map<String,String> handle(ResponseStatusException ex) {
        Map<String,String> error = new HashMap<>();
        error.put("message", ex.getReason());
        return error;
    }

    @ExceptionHandler(Exception.class)
    public Map<String,String> handleGeneral(Exception ex) {
        Map<String,String> error = new HashMap<>();
        error.put("message", "Something went wrong");
        return error;
    }
}