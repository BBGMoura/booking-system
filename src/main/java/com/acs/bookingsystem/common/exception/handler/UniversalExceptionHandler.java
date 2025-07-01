package com.acs.bookingsystem.common.exception.handler;

import com.acs.bookingsystem.common.exception.AuthorizationException;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.common.exception.model.ErrorModel;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    public ResponseEntity<ErrorModel> handleUserRequestException(RequestException uEx){
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          uEx.getMessage(),
                                          uEx.getError().toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorModel>> handleFieldValidation(MethodArgumentNotValidException maEx){
        List<ErrorModel> errors = maEx.getBindingResult()
                                      .getFieldErrors()
                                      .stream()
                                      .map(fieldError -> new ErrorModel(new Date(),
                                                                        HttpStatus.BAD_REQUEST.value(),
                                                                        fieldError.getDefaultMessage(),
                                                                        fieldError.getField()))
                                      .toList();
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorModel> handleNotFound(NotFoundException nfEx){
        ErrorModel error = new ErrorModel(new Date(),
                                            HttpStatus.NOT_FOUND.value(),
                                            nfEx.getMessage(),
                                            nfEx.getError().toString());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorModel> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        final Throwable mostSpecificCause = ex.getMostSpecificCause();
        final ErrorModel errorModel = new ErrorModel(new Date(),
                                               HttpStatus.BAD_REQUEST.value(),
                                               getErrorMessage(ex.getCause()),
                                               mostSpecificCause.getClass().getName());

        LOG.debug("JSON parse error: {}", mostSpecificCause.getMessage());

        return new ResponseEntity<>(errorModel, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorModel> handleInvalidAuthorization(AuthorizationException authEx) {
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.FORBIDDEN.value(),
                                          authEx.getError()
                                                .getDescription(),
                                          authEx.getError().toString());

        if (LOG.isErrorEnabled()) {
            LOG.error("Authorization Exception caused by: {}. Stack trace: {}",
                      authEx.getCause(),
                      Arrays.toString(authEx.getStackTrace()));
        }


        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorModel> handleAuthenticationException(AuthenticationException exception) {
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.FORBIDDEN.value(),
                                          determineAuthenticationErrorMessage(exception),
                                          exception.getMessage());

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    private static String getErrorMessage(Throwable ex) {
        if (ex instanceof InvalidFormatException ifEx && ifEx.getTargetType().isEnum()) {
            return String.format("%s is not a valid class type. Must be one of : %s",
                                 ifEx.getValue(),
                                 Arrays.toString(ifEx.getTargetType().getEnumConstants()));
        }
        return ErrorCode.INTERNAL_ERROR.getDescription();
    }

    private String determineAuthenticationErrorMessage(AuthenticationException exception) {
        return switch (exception) {
            case BadCredentialsException badCredentialsException -> "Invalid email or password.";
            case LockedException lockedException -> "Account is locked. Please contact support.";
            case DisabledException disabledException -> "Account is disabled. Please contact support.";
            case null -> "Authentication failed: Unknown error.";
            default -> "Authentication failed: " + exception.getMessage();
        };
    }

}
