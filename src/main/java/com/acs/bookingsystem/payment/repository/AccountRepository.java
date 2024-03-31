package com.acs.bookingsystem.payment.repository;

import com.acs.bookingsystem.payment.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Integer> {
}
