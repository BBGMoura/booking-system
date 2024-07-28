package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InvitationResponse;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.request.AuthenticationRequest;
import com.acs.bookingsystem.user.request.RegisterRequest;
import com.acs.bookingsystem.user.response.AuthenticationResponse;
import com.acs.bookingsystem.user.response.RegistrationResponse;

public interface AuthenticationService {
    InvitationResponse invite(InviteRequest request);
    RegistrationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
