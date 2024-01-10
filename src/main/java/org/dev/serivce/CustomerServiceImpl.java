package org.dev.serivce;

import org.dev.dto.CustomerDTO;
import org.dev.repository.CustomerRepository;

import java.util.List;

public class CustomerServiceImpl implements CustomerService{

    private CustomerRepository customerRepository;

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDto) {
        return null;
    }

    @Override
    public CustomerDTO getCustomerById(int customerId) {
        return null;
    }

    @Override
    public CustomerDTO getByEmail(String customerEmail) {
        return null;
    }

    @Override
    public List<CustomerDTO> getByFirstName(String firstName) {
        return null;
    }

    @Override
    public List<CustomerDTO> getByLastName(String lastName) {
        return null;
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return null;
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDto) {
        return null;
    }

    @Override
    public void deleteCustomer(int customerId) {

    }
}
