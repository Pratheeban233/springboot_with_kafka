package com.tutorial.kafka.SpringBootwithKafka.controller;

import com.tutorial.kafka.SpringBootwithKafka.domain.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class LibraryEventControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> getRequestValidationError(MethodArgumentNotValidException ex) {

        List<FieldError> errorList = ex.getBindingResult().getFieldErrors();
        String errors = errorList.stream()
                .map(fieldError -> fieldError.getField() + "-" + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.info("Error List : {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
