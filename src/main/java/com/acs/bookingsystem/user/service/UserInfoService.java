package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;

public interface UserInfoService {
    UserInfo createUserInfo(UserInfo userInfo);
    UserInfo updateUserInfo(int userId, UpdateUserInfoRequest request);
}
