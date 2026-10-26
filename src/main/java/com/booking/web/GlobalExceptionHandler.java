package com.booking.web;

import com.booking.service.BusinessRuleException;
import com.booking.service.ConflictException;
import com.booking.service.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ProblemDetail conflict(Exception e) {
        String message = e instanceof ConflictException ? e.getMessage() : "Já existe um registro com esses dados.";
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail rule(BusinessRuleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail badCredentials(BadCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail denied(AccessDeniedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException e) {
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
    }
}
