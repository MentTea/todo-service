package com.simplesystem.todoservice.api;

import com.simplesystem.todoservice.api.model.ErrorResponse;
import com.simplesystem.todoservice.exception.DueDateInThePastException;
import com.simplesystem.todoservice.exception.PastDueModificationNotAllowedException;
import com.simplesystem.todoservice.exception.PastDueToUpdateStatusException;
import com.simplesystem.todoservice.exception.TodoNotFoundException;
import lombok.val;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TodoNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DueDateInThePastException.class)
    public ResponseEntity<ErrorResponse> handlePastDue(DueDateInThePastException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "DUE_DATE_IN_THE_PAST", ex.getMessage());
    }

    @ExceptionHandler(PastDueToUpdateStatusException.class)
    public ResponseEntity<ErrorResponse> handlePastDue(PastDueToUpdateStatusException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "PAST_DUE_TO_UPDATE_STATUS", ex.getMessage());
    }

    @ExceptionHandler(PastDueModificationNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handlePastDue(PastDueModificationNotAllowedException ex) {
        return buildError(HttpStatus.CONFLICT, "PAST_DUE_MODIFICATION_NOT_ALLOWED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        val message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String code, String message) {
        val body = new ErrorResponse()
                .message(message)
                .code(code)
                .timestamp(OffsetDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
