package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.entity.UserInfo;
import com.acs.bookingsystem.user.request.UpdateUserInfoRequest;

public interface UserInfoService {
    UserInfo getUserInfoByUserId(int userId);
    UserInfo createUserInfo(UserInfo userInfo);
    UserInfo updateUserInfo(int userId, UpdateUserInfoRequest request);
}
