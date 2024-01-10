package org.dev.serivce;

import org.dev.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer getCustomerById(int customerId);
    Customer getByEmail(String customerEmail);
    List<Customer> getByFirstName(String firstName);
    List<Customer> getByLastName(String lastName);
    List<Customer> getAllCustomers();
    Customer updateCustomer(Customer customer);
    void deleteCustomer(int customerId);
}