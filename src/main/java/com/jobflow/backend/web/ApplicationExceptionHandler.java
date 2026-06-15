package com.jobflow.backend.web;

import com.jobflow.backend.application.ApplicationNotFoundException;
import com.jobflow.backend.application.IllegalStatusTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(IllegalStatusTransitionException.class)
    ResponseEntity<ApiError> illegalTransition(IllegalStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("illegal_status_transition", ex.getMessage()));
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    ResponseEntity<ApiError> notFound(ApplicationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("application_not_found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("validation_failed", "Request body did not pass validation."));
    }
}
