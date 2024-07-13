package com.acs.bookingsystem.authorization.service;

import com.acs.bookingsystem.admin.response.InvitationResponse;
import com.acs.bookingsystem.authorization.enums.Permission;
import com.acs.bookingsystem.authorization.request.AuthenticationRequest;
import com.acs.bookingsystem.authorization.request.RegisterRequest;
import com.acs.bookingsystem.authorization.response.AuthenticationResponse;
import com.acs.bookingsystem.authorization.response.RegistrationResponse;

public interface AuthenticationService {
    InvitationResponse inviteUser(String email, Permission permission);
    RegistrationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void verifyUser(String token, int userId);
    String updateEmail(String token, int userId, String updateEmail);
    void changeUserPasswordByToken(String token, String password);
    void resetPassword(String email);
    void setEnabledStatus(String email, boolean enabled);
    void disableUserByToken(String token);
}
