package com.acs.bookingsystem.common.exception;

import com.acs.bookingsystem.booking.exception.DanceClassNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ControllerAdvice
public class UniversalExceptionHandler {
    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ErrorModel> handleUserRequestException(RequestException uEx){
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          uEx.getError().getDescription(),
                                          uEx.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //This will be fed back to user, so error needs to be ready
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorModel>> handleFieldValidation(MethodArgumentNotValidException maEx){
        List<ErrorModel> errors = maEx.getBindingResult()
                                      .getFieldErrors()
                                      .stream()
                                      .map(fieldError -> new ErrorModel(new Date(),
                                                                        HttpStatus.BAD_REQUEST.value(),
                                                                        fieldError.getDefaultMessage(),
                                                                        Arrays.stream(fieldError.getCodes()).findFirst().get()))
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
}
