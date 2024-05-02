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
                           user.isActive(),
                           user.getPermission());
    }

    public User mapDTOToUser(UserDTO userDTO){
        return new User(userDTO.id(),
                        userDTO.firstName(),
                        userDTO.lastName(),
                        userDTO.email(),
                        userDTO.phoneNumber(),
                        userDTO.active(),
                        userDTO.permission(),
                        null);
    }
}
