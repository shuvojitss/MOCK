package com.infy.utility;

import com.infy.exception.InfyVaccineSchedulerException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(InfyVaccineSchedulerException.class)
    public ResponseEntity<ErrorInfo> handleSchedulerException(InfyVaccineSchedulerException ex) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        return buildErrorResponse(ex.getMessage(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorInfo> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleGenericException(Exception ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorInfo> buildErrorResponse(String message, HttpStatus status) {
        ErrorInfo errorInfo = new ErrorInfo(message, status.value(), LocalDateTime.now());
        return new ResponseEntity<>(errorInfo, status);
    }
}
