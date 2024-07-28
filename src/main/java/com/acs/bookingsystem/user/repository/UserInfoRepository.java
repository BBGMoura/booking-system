package com.acs.bookingsystem.user.repository;

import com.acs.bookingsystem.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
}
