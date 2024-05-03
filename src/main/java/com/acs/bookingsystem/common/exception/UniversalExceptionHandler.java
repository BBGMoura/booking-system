package com.acs.bookingsystem.common.exception;

import com.acs.bookingsystem.booking.exception.DanceClassNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ControllerAdvice
public class UniversalExceptionHandler {
    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ErrorModel> handleUserRequestException(RequestException uEx){
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          uEx.getMessage(),
                                          uEx.getError().toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //This will be fed back to user, so error message string needs to be ready
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

    @ExceptionHandler(DanceClassNotFoundException.class)
    public ResponseEntity<ErrorModel> handleDanceClassNotFound(DanceClassNotFoundException dEx){
        ErrorModel error = new ErrorModel(new Date(),
                                            HttpStatus.NOT_FOUND.value(),
                                            dEx.getMessage(),
                                            dEx.getError().getDescription());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorModel> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        final Throwable mostSpecificCause = ex.getMostSpecificCause();
        final ErrorModel errorModel = new ErrorModel(new Date(),
                                               HttpStatus.BAD_REQUEST.value(),
                                               getErrorMessage(ex.getCause()),
                                               mostSpecificCause.getClass().getName());

        //debug.error
        System.out.println("JSON parse error: " + mostSpecificCause.getMessage());

        return new ResponseEntity<>(errorModel, HttpStatus.BAD_REQUEST);
    }

    private static String getErrorMessage(Throwable ex) {
        if (ex instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            return String.format("%s is not a valid class type. Must be one of : %s",
                                 ife.getValue(),
                                 Arrays.toString(ife.getTargetType().getEnumConstants()));
        }
        return ErrorCode.INTERNAL_ERROR.getDescription();
    }
}
