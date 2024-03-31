package com.acs.bookingsystem.user.repository;

import com.acs.bookingsystem.user.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
