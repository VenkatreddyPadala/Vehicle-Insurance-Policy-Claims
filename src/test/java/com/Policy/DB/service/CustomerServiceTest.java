package com.Policy.DB.service;

import com.Policy.DB.model.Customer;
import com.Policy.DB.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // -------- Register Customer --------
    @Test
    void registerCustomer_success() {
        Customer customer = new Customer(
                1, "Venkat", "venkat@gmail.com",
                "9876543210", "Hyderabad", null
        );

        Mockito.when(customerRepository.existsByEmail(customer.getEmail()))
                .thenReturn(false);
        Mockito.when(customerRepository.save(customer))
                .thenReturn(customer);

        Customer savedCustomer = customerService.registerCustomer(customer);

        assertNotNull(savedCustomer);
        assertEquals("Venkat", savedCustomer.getName());
        assertEquals("venkat@gmail.com", savedCustomer.getEmail());
    }

    @Test
    void registerCustomer_emailAlreadyExists() {
        Customer customer = new Customer(
                1, "Venkat", "venkat@gmail.com",
                "9876543210", "Hyderabad", null
        );

        Mockito.when(customerRepository.existsByEmail(customer.getEmail()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.registerCustomer(customer)
        );

        assertEquals("Customer with this email already exists", exception.getMessage());
    }

    // -------- Update Customer --------
    @Test
    void updateCustomerProfile_success() {
        Customer existingCustomer = new Customer(
                1, "Venkat", "old@gmail.com",
                "9876543210", "Old Address", null
        );

        Customer updatedDetails = new Customer(
                null, "Updated Name", "updated@gmail.com",
                "9999999999", "New Address", null
        );

        Mockito.when(customerRepository.findById(1))
                .thenReturn(Optional.of(existingCustomer));
        Mockito.when(customerRepository.save(existingCustomer))
                .thenReturn(existingCustomer);

        Customer updatedCustomer =
                customerService.updateCustomerProfile(1, updatedDetails);

        assertEquals("Updated Name", updatedCustomer.getName());
        assertEquals("updated@gmail.com", updatedCustomer.getEmail());
        assertEquals("9999999999", updatedCustomer.getPhone());
        assertEquals("New Address", updatedCustomer.getAddress());
    }

    @Test
    void updateCustomerProfile_notFound() {
        Mockito.when(customerRepository.findById(99))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.updateCustomerProfile(99, new Customer())
        );

        assertEquals("Customer not found with id: 99", exception.getMessage());
    }

    // -------- Get Customer Details --------
    @Test
    void getCustomerDetails_success() {
        Customer customer = new Customer(
                1, "Venkat", "venkat@gmail.com",
                "9876543210", "Hyderabad", null
        );

        Mockito.when(customerRepository.findById(1))
                .thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerDetails(1);

        assertNotNull(result);
        assertEquals("Venkat", result.getName());
        assertEquals("venkat@gmail.com", result.getEmail());
    }

    @Test
    void getCustomerDetails_notFound() {
        Mockito.when(customerRepository.findById(99))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.getCustomerDetails(99)
        );

        assertEquals("Customer not found with id: 99", exception.getMessage());
    }

    // -------- Get All Customers --------
    @Test
    void getAllCustomers_success() {
        List<Customer> customers = List.of(
                new Customer(1, "Venkat", "v@gmail.com", "123", "Hyd", null),
                new Customer(2, "Ravi", "r@gmail.com", "456", "Delhi", null)
        );

        Mockito.when(customerRepository.findAll())
                .thenReturn(customers);

        List<Customer> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertEquals("Venkat", result.get(0).getName());
    }

    // -------- Delete Customer --------
    @Test
    void deleteCustomer_success() {
        Mockito.when(customerRepository.existsById(1))
                .thenReturn(true);

        customerService.deleteCustomer(1);

        Mockito.verify(customerRepository, Mockito.times(1))
                .deleteById(1);
    }

    @Test
    void deleteCustomer_notFound() {
        Mockito.when(customerRepository.existsById(99))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.deleteCustomer(99)
        );

        assertEquals("Customer not found with id: 99", exception.getMessage());
    }

    // -------- Find By Email --------
    @Test
    void findByEmail_success() {
        Customer customer = new Customer(
                1, "Venkat", "venkat@gmail.com",
                "9876543210", "Hyderabad", null
        );

        Mockito.when(customerRepository.findByEmail("venkat@gmail.com"))
                .thenReturn(Optional.of(customer));

        Optional<Customer> result =
                customerService.findByEmail("venkat@gmail.com");

        assertTrue(result.isPresent());
        assertEquals("Venkat", result.get().getName());
    }
}
