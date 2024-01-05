package org.dev.repository;

import org.dev.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);

    Iterable<Customer> findByFirstName(String firstName);

    Iterable<Customer> findByLastName(String lastName);

    Iterable<Customer> findByFirstNameAndLastName(String firstName, String lastName);
}
