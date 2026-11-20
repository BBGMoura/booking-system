package com.acs.bookingsystem.user.repository;

import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmail(String email);

  Optional<User> findByUid(UUID uid);

  boolean existsByRole(Role role);
}
