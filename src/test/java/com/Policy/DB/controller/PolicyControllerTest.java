package com.Policy.DB.controller;

import com.Policy.DB.model.Policy;
import com.Policy.DB.model.PolicyStatus;
import com.Policy.DB.service.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------- Create Policy --------
    @Test
    void createPolicy_success() throws Exception {
        Policy policy = new Policy(
                1, null, "POL123",
                new BigDecimal("500000"),
                new BigDecimal("2500"),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                PolicyStatus.ACTIVE,
                null
        );

        Mockito.when(policyService.createPolicy(Mockito.any(Policy.class), Mockito.eq(1)))
                .thenReturn(policy);

        mockMvc.perform(post("/policies/create/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policy)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyNumber").value("POL123"));
    }

    // -------- Get Policy By ID --------
    @Test
    void getPolicyDetails_success() throws Exception {
        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setPolicyNumber("POL123");

        Mockito.when(policyService.getPolicyDetails(1)).thenReturn(policy);

        mockMvc.perform(get("/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNumber").value("POL123"));
    }

    // -------- Renew Policy --------
    @Test
    void renewPolicy_success() throws Exception {
        Policy policy = new Policy();
        policy.setPolicyStatus(PolicyStatus.ACTIVE);

        Mockito.when(policyService.renewPolicy(Mockito.eq(1), Mockito.any(LocalDate.class)))
                .thenReturn(policy);

        mockMvc.perform(put("/policies/renew/1")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk());
    }

    // -------- Get All Policies --------
    @Test
    void getAllPolicies_success() throws Exception {
        Mockito.when(policyService.getAllPolicies())
                .thenReturn(List.of(new Policy(), new Policy()));

        mockMvc.perform(get("/policies/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    // -------- Get Policies By Customer --------
    @Test
    void getPoliciesByCustomer_success() throws Exception {
        Mockito.when(policyService.getPoliciesByCustomerId(1))
                .thenReturn(List.of(new Policy()));

        mockMvc.perform(get("/policies/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // -------- Get Active Policies --------
    @Test
    void getActivePolicies_success() throws Exception {
        Mockito.when(policyService.getActivePolicies())
                .thenReturn(List.of(new Policy()));

        mockMvc.perform(get("/policies/active"))
                .andExpect(status().isOk());
    }

    // -------- Get Expired Policies --------
    @Test
    void getExpiredPolicies_success() throws Exception {
        Mockito.when(policyService.getExpiredPolicies())
                .thenReturn(List.of(new Policy()));

        mockMvc.perform(get("/policies/expired"))
                .andExpect(status().isOk());
    }
}
