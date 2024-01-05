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
        Customer customer = createTestCustomer();

        //when
        customerRepository.save(customer);

        int customerId = customer.getId();
        Customer savedCustomer = customerRepository.findById(customerId).orElseThrow();

        List<Customer> savedCustomers = new ArrayList<>();
        customerRepository.findAll().forEach(savedCustomers::add);

        //then
        assertEquals(customerId, savedCustomer.getId());
        assertEquals(customer.getEmail(), savedCustomer.getEmail());
        assertEquals(customer.getFirstName(), savedCustomer.getFirstName());
        assertEquals(customer.getLastName(), savedCustomer.getLastName());
        assertEquals(1, customerRepository.count());
        assertEquals(1, savedCustomers.size());
    }


    private Customer createTestCustomer() {
        return new Customer("Lara", "Jean", "larajean@email.com");
    }
}