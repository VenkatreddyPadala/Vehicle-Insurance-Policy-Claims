package com.Policy.DB.controller;

import com.Policy.DB.model.Customer;
import com.Policy.DB.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------- Register Customer --------
    @Test
    void registerCustomer_success() throws Exception {
        Customer customer = new Customer(1, "Venkat", "venkat@gmail.com",
                "9876543210", "Hyderabad", null);

        Mockito.when(customerService.registerCustomer(Mockito.any(Customer.class)))
                .thenReturn(customer);

        mockMvc.perform(post("/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Venkat"))
                .andExpect(jsonPath("$.email").value("venkat@gmail.com"));
    }

    // -------- Update Customer --------
    @Test
    void updateCustomer_success() throws Exception {
        Customer updatedCustomer = new Customer(1, "Updated Name",
                "updated@gmail.com", "9999999999", "New Address", null);

        Mockito.when(customerService.updateCustomerProfile(Mockito.eq(1), Mockito.any(Customer.class)))
                .thenReturn(updatedCustomer);

        mockMvc.perform(put("/customers/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    // -------- Get Customer By ID --------
    @Test
    void getCustomerDetails_success() throws Exception {
        Customer customer = new Customer(1, "Venkat",
                "venkat@gmail.com", "9876543210", "Hyderabad", null);

        Mockito.when(customerService.getCustomerDetails(1)).thenReturn(customer);

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("venkat@gmail.com"));
    }

    // -------- Get All Customers --------
    @Test
    void getAllCustomers_success() throws Exception {
        List<Customer> customers = List.of(
                new Customer(1, "Venkat", "v@gmail.com", "123", "Hyd", null),
                new Customer(2, "Ravi", "r@gmail.com", "456", "Delhi", null)
        );

        Mockito.when(customerService.getAllCustomers()).thenReturn(customers);

        mockMvc.perform(get("/customers/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    // -------- Delete Customer --------
    @Test
    void deleteCustomer_success() throws Exception {
        Mockito.doNothing().when(customerService).deleteCustomer(1);

        mockMvc.perform(delete("/customers/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer deleted successfully"));
    }

    // -------- Delete Customer Not Found --------
    @Test
    void deleteCustomer_notFound() throws Exception {
        Mockito.doThrow(new RuntimeException("Customer not found"))
                .when(customerService).deleteCustomer(99);

        mockMvc.perform(delete("/customers/delete/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void registerCustomer_exception() throws Exception {
        Customer customer = new Customer(1, "Venkat",
                "venkat@gmail.com", "9876543210", "Hyderabad", null);

        Mockito.when(customerService.registerCustomer(Mockito.any(Customer.class)))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void updateCustomer_notFound() throws Exception {
        Customer customer = new Customer(1, "Venkat",
                "v@gmail.com", "999", "Hyd", null);

        Mockito.when(customerService.updateCustomerProfile(Mockito.eq(99), Mockito.any(Customer.class)))
                .thenThrow(new RuntimeException("Customer not found"));

        mockMvc.perform(put("/customers/update/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isNotFound());
    }
    @Test
    void getCustomerDetails_notFound() throws Exception {
        Mockito.when(customerService.getCustomerDetails(99))
                .thenThrow(new RuntimeException("Customer not found"));

        mockMvc.perform(get("/customers/99"))
                .andExpect(status().isNotFound());
    }
}
