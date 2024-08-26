package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.request.*;
import com.acs.bookingsystem.user.response.InvitateResponse;
import com.acs.bookingsystem.user.response.AuthenticateResponse;
import com.acs.bookingsystem.user.response.RegistrateResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;

public interface AuthenticateService {
    InvitateResponse invite(InviteRequest request);
    RegistrateResponse register(RegisterRequest request);
    AuthenticateResponse authenticate(AuthenticateRequest request);
    void updateUserCredentials(int userId, UpdateUserRequest request);
    UserStatusResponse updatedEnabledStatus(int userId, boolean status);
    void resetPassword(String email);
}
