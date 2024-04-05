package com.acs.bookingsystem.user.mapper;

import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.entities.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public UserDTO mapUserToDTO(User user) {
        return new UserDTO(user.getId(),
                           user.getFirstName(),
                           user.getLastName(),
                           user.getEmail(),
                           user.getPhoneNumber(),
                           user.getPermission());
    }
}
