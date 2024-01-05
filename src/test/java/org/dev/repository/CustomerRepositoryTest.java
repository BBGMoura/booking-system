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
        final Customer customer = createTestCustomer();

        //when
        customerRepository.save(customer);

        final int customerId = customer.getId();
        final Customer savedCustomer = customerRepository.findById(customerId).orElseThrow();

        final List<Customer> savedCustomers = new ArrayList<>();
        customerRepository.findAll().forEach(savedCustomers::add);

        //then
        assertEquals(customerId, savedCustomer.getId());
        assertEquals(customer.getEmail(), savedCustomer.getEmail());
        assertEquals(customer.getFirstName(), savedCustomer.getFirstName());
        assertEquals(customer.getLastName(), savedCustomer.getLastName());
        assertEquals(1, customerRepository.count());
        assertEquals(1, savedCustomers.size());
    }

    @Test
    void testFindByEmail(){
        //given
        final Customer customer = createTestCustomer();
        customerRepository.save(customer);

        //when
        final Customer retrievedCustomer = customerRepository.findByEmail("larajean@email.com").orElseThrow();

        //then
        assertEquals(customer, retrievedCustomer);
        assertEquals(customer.getFirstName(), retrievedCustomer.getFirstName());
        assertEquals(customer.getLastName(), retrievedCustomer.getLastName());
    }

    @Test
    void testFindByFirstName() {
        //given
        final Customer customer = createTestCustomer();
        customerRepository.save(customer);

        //when
        final List<Customer> retrievedCustomers = new ArrayList<>();
        customerRepository.findByFirstName("Lara").forEach(retrievedCustomers::add);

        //then
        assertEquals(1, retrievedCustomers.size());
    }

    @Test
    void testUpdate() {
        //given
        final Customer customer = createTestCustomer();
        customerRepository.save(customer);

        //when
        customer.setFirstName("Updated");
        customer.setLastName("Customer");
        customer.setEmail("updatedemail@email.com");
        customerRepository.save(customer);

        final Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();

        //then
        assertEquals(customer.getId(), updatedCustomer.getId());
        assertEquals("Updated", updatedCustomer.getFirstName());
        assertEquals("Customer", updatedCustomer.getLastName());
        assertEquals("updatedemail@email.com", updatedCustomer.getEmail());
    }


    private Customer createTestCustomer() {
        return new Customer("Lara", "Jean", "larajean@email.com");
    }
}