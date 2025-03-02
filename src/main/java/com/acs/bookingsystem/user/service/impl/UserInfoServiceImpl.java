package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.repository.UserInfoRepository;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.service.UserInfoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private UserInfoRepository userInfoRepository;

    @Override
    public UserInfo getUserInfoByUserId(int userId) {
        return getUserInfo(userId);
    }

    @Override
    public UserInfo createUserInfo(UserInfo userInfo) {
        userInfoRepository.findByUserId(userInfo.getUser().getId())
                          .ifPresent( info -> {
                              throw new RequestException("User info with email: " + info.getUser().getUsername() + " already exists.",
                                      ErrorCode.INTERNAL_ERROR);
                          });

        return userInfoRepository.save(userInfo);
    }

    @Override
    public UserInfo updateUserInfo(int userId, UpdateUserInfoRequest request) {
        final UserInfo userInfo = getUserInfo(userId);

        if (request.firstName() != null && !request.firstName().isBlank()) {
            userInfo.setFirstName(request.firstName());
        }

        if (request.lastName() != null && !request.lastName().isBlank()) {
            userInfo.setLastName(request.lastName());
        }

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            userInfo.setPhoneNumber(request.phoneNumber());
        }

        return userInfoRepository.save(userInfo);
    }

    private UserInfo getUserInfo(int userId) {
        return userInfoRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RequestException("Cannot find user. Please contact support.",
                                ErrorCode.INVALID_USER_ID)
                );
    }
}
