package org.dev.repository;

import org.dev.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testSave() {
        //given
        Customer customer = new Customer(1, "Lara", "Jean", "larajean@email.com");

        //when
        customerRepository.save(customer);

        int customerId = customer.getId();
        Customer retrievedCustomer = customerRepository.findById(customerId).orElseThrow();

        List<Customer> retrievedCustomers = new ArrayList<>();
        customerRepository.findAll().forEach(retrievedCustomers::add);

        //then
        assertEquals(customerId, retrievedCustomer.getId());
        assertEquals(customer.getEmail(), retrievedCustomer.getEmail());
        assertEquals(customer.getFirstName(), retrievedCustomer.getFirstName());
        assertEquals(customer.getLastName(), retrievedCustomer.getLastName());
        assertEquals(1, customerRepository.count());
        assertEquals(1, retrievedCustomers.size());
    }
}