package org.dev.serivce;

import org.dev.dto.CustomerDTO;
import org.dev.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService{
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerDTO createCustomer(final CustomerDTO customerDto) {
        return null;
    }

    @Override
    public CustomerDTO getCustomerById(final int customerId) {
        return null;
    }

    @Override
    public CustomerDTO getByEmail(final String customerEmail) {
        return null;
    }

    @Override
    public List<CustomerDTO> getByFirstName(final String firstName) {
        return null;
    }

    @Override
    public List<CustomerDTO> getByLastName(final String lastName) {
        return null;
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return null;
    }

    @Override
    public CustomerDTO updateCustomer(final int customerId, final CustomerDTO customerDto) {
        return null;
    }

    @Override
    public void deleteCustomer(int customerId) {
//
    }
}
