package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.service.UserInfoService;
import com.acs.bookingsystem.user.service.UserProfileService;
import com.acs.bookingsystem.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private UserService userService;
    private UserInfoService userInfoService;

    @Override
    public UserProfile getUserProfile(int userId) {
        final UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);

        final User user = userService.getUserById(userId);

        return createUserProfile(user, userInfo);
    }

    @Override
    public Page<UserProfile> getUserProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("user.lastName").ascending());

        Page<User> userPage = userService.getUsers(pageable);

        List<UserProfile> userProfiles = userPage.getContent()
                                                 .stream()
                                                 .map(user -> {
                                                     UserInfo userInfo = userInfoService.getUserInfoByUserId(user.getId());
                                                     return createUserProfile(user, userInfo);
                                                 })
                                                 .toList();

        return new PageImpl<>(userProfiles, pageable,userPage.getTotalElements());
    }

    private UserProfile createUserProfile(User user, UserInfo userInfo){
        return UserProfile.builder()
                .userId(user.getId())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .phoneNumber(userInfo.getPhoneNumber())
                .email(user.getUsername())
                .status(user.isEnabled())
                .permission(user.getPermission())
                .build();
    }

}
