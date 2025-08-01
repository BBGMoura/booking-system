package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.service.UserInfoService;
import com.acs.bookingsystem.user.service.UserProfileService;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserService userService;
    private final UserInfoService userInfoService;

    @Override
    public UserProfile getUserProfile(int userId) {
        final UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);

        final User user = userService.getUserById(userId);

        return createUserProfile(user, userInfo);
    }

    @Override
    public Page<UserProfile> getUserProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = userService.getUsers(pageable);

        List<UserProfile> userProfiles = userPage.getContent()
                                                 .stream()
                                                 .map(user -> {
                                                     UserInfo userInfo = userInfoService.getUserInfoByUserId(user.getId());
                                                     return createUserProfile(user, userInfo);
                                                 })
                                                 .sorted(Comparator.comparing(UserProfile::lastName))
                                                 .toList();

        return new PageImpl<>(userProfiles, pageable, userPage.getTotalElements());
    }

    private UserProfile createUserProfile(User user, UserInfo userInfo){
        return UserProfile.builder()
                .userId(user.getId())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .phoneNumber(userInfo.getPhoneNumber())
                .email(user.getUsername())
                .enabled(user.isEnabled())
                .role(user.getRole())
                .build();
    }
}
