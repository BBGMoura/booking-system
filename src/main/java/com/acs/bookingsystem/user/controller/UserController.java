package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.authorization.service.AuthenticationService;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.request.UserRequest;
import com.acs.bookingsystem.user.request.UserUpdateRequest;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Validated
public class UserController {
    private UserService userService;
    private AuthenticationService authenticationService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@RequestHeader(name = "Authorization") String token,
                                               @PathVariable int userId) {
        authenticationService.verifyUser(token, userId);
        //TODO: should return an object which includes auth user and user for showing. maybe this could be UserDetailDTO
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestHeader(name = "Authorization") String token,
                                              @PathVariable int userId,
                                              @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        authenticationService.verifyUser(token, userId);
        return ResponseEntity.ok(userService.updateUser(userId, userUpdateRequest));
    }

    @PatchMapping("/update/{userId}/email")
    public ResponseEntity<String> updateUserEmail(@RequestHeader(name = "Authorization") String token,
                                                  @PathVariable int userId,
                                                  @Pattern(regexp="[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", message="Invalid email format") String updatedEmail) {
        return ResponseEntity.ok(authenticationService.updateEmail(token, userId, updatedEmail));
    }

    @PatchMapping("/disable")
    public ResponseEntity<Void> disableUser(@RequestHeader(name = "Authorization") String token){
        authenticationService.disableUserByToken(token);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> updateUserPassword(@RequestHeader(name = "Authorization") String token,
                                                   @RequestParam String password) {
        authenticationService.changeUserPasswordByToken(token, password);
        return ResponseEntity.noContent().build();
    }
}
