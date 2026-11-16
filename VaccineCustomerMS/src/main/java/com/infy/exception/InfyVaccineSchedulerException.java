package com.infy.exception;

import org.springframework.http.HttpStatus;

public class InfyVaccineSchedulerException extends Exception {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    public InfyVaccineSchedulerException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public InfyVaccineSchedulerException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
