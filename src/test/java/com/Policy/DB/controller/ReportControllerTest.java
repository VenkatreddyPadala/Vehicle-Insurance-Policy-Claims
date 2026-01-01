package com.Policy.DB.controller;

import com.Policy.DB.service.ReportService;
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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------- Claim Report --------
    @Test
    void generateClaimReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalClaims", 5);
        report.put("approvedClaimAmount", BigDecimal.valueOf(10000));

        Mockito.when(reportService.generateClaimReport()).thenReturn(report);

        mockMvc.perform(get("/reports/claims")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClaims").value(5));
    }

    // -------- Policy Report --------
    @Test
    void generatePolicyReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 3);
        report.put("activePolicies", 2);

        Mockito.when(reportService.generatePolicyReport()).thenReturn(report);

        mockMvc.perform(get("/reports/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(3));
    }

    // -------- Customer Report --------
    @Test
    void generateCustomerReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 2);
        report.put("totalClaims", 1);

        Mockito.when(reportService.generateCustomerReport(1)).thenReturn(report);

        mockMvc.perform(get("/reports/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(2));
    }
}
