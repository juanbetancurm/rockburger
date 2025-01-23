package com.rockburger.arquetipo2024.configuration.exceptionhandler;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.exception.*;
import com.rockburger.arquetipo2024.configuration.Constants;
import com.rockburger.arquetipo2024.domain.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.rockburger.arquetipo2024.configuration.Constants.*;

@ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvisor {
    @ExceptionHandler(EmptyFieldException.class)
    public ResponseEntity<Map<String, String>> handleEmptyFieldException(EmptyFieldException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(BLANK_FIELD_ERROR, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NegativeNotAllowedException.class)
    public ResponseEntity<ExceptionResponse> handleNegativeNotAllowedException(NegativeNotAllowedException exception) {
        return ResponseEntity.badRequest().body(new ExceptionResponse(
                String.format(Constants.NEGATIVE_NOT_ALLOWED_EXCEPTION_MESSAGE, exception.getMessage()),
                HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now()));
    }


    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoDataFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(
                Constants.NO_DATA_FOUND_EXCEPTION_MESSAGE, HttpStatus.NOT_FOUND.toString(), LocalDateTime.now()));
    }
    @ExceptionHandler(ElementNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleElementNotFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(
                Constants.ELEMENT_NOT_FOUND_EXCEPTION_MESSAGE, HttpStatus.CONFLICT.toString(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NameAlreadyExistsExceptionD.class)
    public ResponseEntity<Map<String, String>> handleNameAlreadyExistException(NameAlreadyExistsExceptionD ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(NAME_ALREADY_EXIST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(BlankFieldException.class)
    public ResponseEntity<Map<String, String>> handleCategoryInvalidNameException(BlankFieldException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(EMPTY_FIELD_EXCEPTION_MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new LinkedHashMap<>();
        errorResponse.put(Constants.ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPageParameterException(InvalidParameterException ex) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put(Constants.INVALID_PAGE_PARAMETER_MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateCategoryException(DuplicateCategoryException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(DUPLICATE_CATEGORY_MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCategoryCountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCategoryCountException(InvalidCategoryCountException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(INVALID_CATEGORY_COUNT_MESSAGE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidAgeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAgeException(InvalidAgeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid Age");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateUserException(DuplicateUserException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Duplicate User");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }





    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidCredentialsException(
            InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(
                        exception.getMessage(),
                        HttpStatus.UNAUTHORIZED.toString(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ExceptionResponse> handleTokenExpiredException(
            TokenExpiredException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(
                        exception.getMessage(),
                        HttpStatus.UNAUTHORIZED.toString(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidTokenException(
            InvalidTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(
                        exception.getMessage(),
                        HttpStatus.UNAUTHORIZED.toString(),
                        LocalDateTime.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(
            AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse(
                        Constants.INSUFFICIENT_PERMISSIONS_MESSAGE,
                        HttpStatus.FORBIDDEN.toString(),
                        LocalDateTime.now()));
    }

}
