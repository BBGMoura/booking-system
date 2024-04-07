package com.acs.bookingsystem.user.exception;

import com.acs.bookingsystem.user.controller.UserController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ControllerAdvice(assignableTypes = {UserController.class})
public class UserExceptionHandler {
    @ExceptionHandler(UserRequestException.class)
    public ResponseEntity<ErrorModel> handleUserRequestException(UserRequestException uEx){
        ErrorModel error = new ErrorModel(new Date(),
                                          HttpStatus.BAD_REQUEST.value(),
                                          uEx.getError().getDescription(),
                                          uEx.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //This will be fed back to user, so needs to be ready
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
}
