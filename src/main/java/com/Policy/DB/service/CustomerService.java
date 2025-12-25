package com.Policy.DB.service;

import com.Policy.DB.model.Customer;
import com.Policy.DB.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    // For Registering New Customer
    public Customer registerCustomer(Customer customer){
        if(customerRepository.existsByEmail(customer.getEmail())){
            throw new RuntimeException("Customer with this email already exists");
        }
        return customerRepository.save(customer);
    }

    // Update Customer Profile
    public Customer updateCustomerProfile(Integer customerId, Customer customerDetails) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());

        return customerRepository.save(customer);
    }

    // Get Customer Details
    public Customer getCustomerDetails(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
    }

    //Get all Customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    //Delete Customer
    public void deleteCustomer(Integer customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("Customer not found with id: " + customerId);
        }
        customerRepository.deleteById(customerId);
    }

    // Find by email
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
}
