package com.acs.bookingsystem.user.service;

import com.acs.bookingsystem.user.model.UserProfile;
import org.springframework.data.domain.Page;

public interface UserProfileService {
    UserProfile getUserProfile(int userId);
    Page<UserProfile> getUserProfiles(int page, int size);
}
