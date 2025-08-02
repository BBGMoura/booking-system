package com.acs.bookingsystem.user.controller;

import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-info")
@RequiredArgsConstructor
@Validated
public class UserInfoController {

    private final UserInfoService userInfoService;

    @PutMapping("/userId")
    public ResponseEntity<UserInfo> updateUserInfo(@CurrentUser User user,
                                                   @Valid @RequestBody UpdateUserInfoRequest request){
        return ResponseEntity.ok(userInfoService.updateUserInfo(user.getId(), request));
    }
}
