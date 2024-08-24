package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.request.*;
import com.acs.bookingsystem.user.response.InvitationResponse;
import com.acs.bookingsystem.user.response.AuthenticationResponse;
import com.acs.bookingsystem.user.response.RegistrationResponse;
import com.acs.bookingsystem.user.response.UserStatusResponse;

public interface AuthenticateService {
    InvitationResponse invite(InviteRequest request);
    RegistrationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void updateUserCredentials(int userId, UpdateUserRequest request);
    UserStatusResponse updatedEnabledStatus(int userId, boolean status);
    void resetPassword(String email);
}
