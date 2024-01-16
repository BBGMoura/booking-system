package org.dev.serivce;

import org.dev.dto.CustomerDTO;
import org.dev.model.Customer;

import java.util.List;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerDTO customerDto);
    CustomerDTO getCustomerById(int customerId);
    CustomerDTO getByEmail(String customerEmail);
    List<CustomerDTO> getByFirstName(String firstName);
    List<CustomerDTO> getByLastName(String lastName);
    List<CustomerDTO> getAllCustomers();
    CustomerDTO updateCustomer(int customerId, CustomerDTO customerDto);
    void deleteCustomer(int customerId);
}