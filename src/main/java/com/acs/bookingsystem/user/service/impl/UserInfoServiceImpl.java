package com.acs.bookingsystem.user.service.impl;

import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.repository.UserInfoRepository;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;
import com.acs.bookingsystem.user.service.UserInfoService;

public class UserInfoServiceImpl implements UserInfoService {
    private UserInfoRepository userInfoRepository;

    @Override
    public UserInfo createUserInfo(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }

    @Override
    public UserInfo updateUserInfo(int userId, UpdateUserInfoRequest request) {
        return null;
    }
}
