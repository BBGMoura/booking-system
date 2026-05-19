package com.acs.bookingsystem.common.exception.handler;

import com.acs.bookingsystem.common.exception.AuthorizationException;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.exception.model.ErrorDetail;
import com.acs.bookingsystem.common.exception.model.ErrorModel;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ControllerAdvice
public class UniversalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UniversalExceptionHandler.class);

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ErrorModel> handleRequestException(RequestException ex) {
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          ex.getError().toString(),
                                          ex.getMessage(),
                                          List.of());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorModel> handleNotFoundException(NotFoundException ex) {
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.NOT_FOUND.value(),
                                          ex.getError().toString(),
                                          ex.getMessage(),
                                          List.of());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorModel> handleFieldValidation(MethodArgumentNotValidException ex) {
        List<ErrorDetail> details = ex.getBindingResult()
                                      .getFieldErrors()
                                      .stream()
                                      .map(fe -> new ErrorDetail(fe.getField(), fe.getDefaultMessage()))
                                      .toList();
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          ErrorCode.VALIDATION_ERROR.toString(),
                                          ErrorCode.VALIDATION_ERROR.getDescription(),
                                          details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorModel> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetail> details = ex.getConstraintViolations()
                                      .stream()
                                      .map(v -> {
                                          String path = v.getPropertyPath().toString();
                                          String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                                          return new ErrorDetail(field, v.getMessage());
                                      })
                                      .toList();
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          ErrorCode.VALIDATION_ERROR.toString(),
                                          ErrorCode.VALIDATION_ERROR.getDescription(),
                                          details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorModel> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOG.debug("JSON parse error: {}", ex.getMostSpecificCause().getMessage());
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          ErrorCode.INVALID_FORMAT.toString(),
                                          getFormatErrorMessage(ex.getCause()),
                                          List.of());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorModel> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        LOG.warn("Optimistic locking conflict on {}: {}", ex.getPersistentClassName(), ex.getMessage());
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.CONFLICT.value(),
                                          ErrorCode.CONFLICT.toString(),
                                          "This request conflicted with another. Please try again.",
                                          List.of());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorModel> handleAuthorizationException(AuthorizationException ex) {
        LOG.warn("Authorization exception: {}", ex.getMessage());
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.FORBIDDEN.value(),
                                          ex.getError().toString(),
                                          ex.getMessage(),
                                          List.of());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorModel> handleAuthenticationException(AuthenticationException ex) {
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.FORBIDDEN.value(),
                                          ErrorCode.AUTHENTICATION_ERROR.toString(),
                                          determineAuthenticationMessage(ex),
                                          List.of());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    private static String getFormatErrorMessage(Throwable cause) {
        if (cause instanceof InvalidFormatException ifEx && ifEx.getTargetType().isEnum()) {
            return String.format("'%s' is not a valid value. Must be one of: %s",
                                 ifEx.getValue(),
                                 Arrays.toString(ifEx.getTargetType().getEnumConstants()));
        }
        return ErrorCode.INVALID_FORMAT.getDescription();
    }

    private static String determineAuthenticationMessage(AuthenticationException ex) {
        return switch (ex) {
            case BadCredentialsException e -> "Invalid email or password.";
            case LockedException e -> "Account is locked. Please contact support.";
            case DisabledException e -> "Account is disabled. Please contact support.";
            case null -> "Authentication failed.";
            default -> "Authentication failed: " + ex.getMessage();
        };
    }
}
