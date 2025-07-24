package com.acme.monitor.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OutOfMemoryError.class)
    public void handleOutOfMemoryError(OutOfMemoryError error) {
        logger.error("JVM OutOfMemoryError caught: ", error);
        // Optionally, you can perform additional actions here, such as sending alerts or metrics
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public void handleDatabaseConnectionFailure(DataAccessResourceFailureException ex) {
        logger.error("Database connection failed: ", ex);
        // Optionally, you can perform additional actions here, such as sending alerts or metrics
    }

    // You can add more exception handlers here for other types of exceptions
}