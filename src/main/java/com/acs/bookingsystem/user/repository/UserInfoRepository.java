package com.acs.bookingsystem.user.repository;

import com.acs.bookingsystem.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    Optional<UserInfo> findByUserId(int userId);
}
