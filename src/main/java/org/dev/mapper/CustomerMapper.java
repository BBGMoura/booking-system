package org.dev.mapper;

import org.dev.dto.CustomerDTO;
import org.dev.model.Customer;

public class CustomerMapper {
    public static CustomerDTO mapToCustomerDTO(Customer customer) {
        return new CustomerDTO(customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail());
    }

    public static Customer mapToCustomer(CustomerDTO customerDTO) {
        return new Customer(customerDTO.getFirstName(),
                customerDTO.getLastName(),
                customerDTO.getEmail());
    }
}
