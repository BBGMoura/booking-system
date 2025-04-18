package com.acs.bookingsystem.user;

import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.model.UserProfile;
import com.acs.bookingsystem.user.request.InviteRequest;
import com.acs.bookingsystem.user.response.InviteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class UserTestData {

    public static final User user = User.builder()
            .email("test@example.com")
            .role(Role.ROLE_USER)
            .locked(false)
            .enabled(true)
            .build();

    public static final UserProfile adminUserProfile = UserProfile.builder()
            .userId(1)
            .email("example@admin.com")
            .firstName("Example")
            .lastName("Admin")
            .role(Role.ROLE_ADMIN)
            .phoneNumber("07300123456")
            .enabled(true)
            .build();

    public static final UserProfile userProfile = UserProfile.builder()
            .userId(1)
            .email("example@user.com")
            .firstName("Example")
            .lastName("User")
            .role(Role.ROLE_USER)
            .phoneNumber("0732235673")
            .enabled(true)
            .build();

    public static final Page<UserProfile> userPage = new PageImpl<>(List.of(adminUserProfile, userProfile));

    public static final InviteRequest inviteRequest = new InviteRequest("example@user.com", Role.ROLE_USER);

    public static final InviteResponse inviteResponse = new InviteResponse(1, "example@user.com", Role.ROLE_USER);
}
