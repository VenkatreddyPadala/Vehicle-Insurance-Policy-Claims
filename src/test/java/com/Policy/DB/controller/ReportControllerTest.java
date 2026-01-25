package com.Policy.DB.controller;

import com.Policy.DB.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================
    // Claim Report Tests (JSON)
    // ============================

    @Test
    void generateClaimReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalClaims", 5);
        report.put("approvedClaimAmount", BigDecimal.valueOf(10000));
        report.put("rejectedClaimAmount", BigDecimal.valueOf(2000));
        report.put("submittedClaimAmount", BigDecimal.valueOf(3000));
        report.put("pendingClaims", 2);

        Mockito.when(reportService.generateClaimReport()).thenReturn(report);

        mockMvc.perform(get("/reports/claims")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClaims").value(5))
                .andExpect(jsonPath("$.approvedClaimAmount").value(10000))
                .andExpect(jsonPath("$.rejectedClaimAmount").value(2000))
                .andExpect(jsonPath("$.submittedClaimAmount").value(3000))
                .andExpect(jsonPath("$.pendingClaims").value(2));

        verify(reportService, times(1)).generateClaimReport();
    }

    @Test
    void generateClaimReport_emptyReport() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalClaims", 0);
        report.put("approvedClaimAmount", BigDecimal.ZERO);
        report.put("rejectedClaimAmount", BigDecimal.ZERO);
        report.put("submittedClaimAmount", BigDecimal.ZERO);
        report.put("pendingClaims", 0);

        Mockito.when(reportService.generateClaimReport()).thenReturn(report);

        mockMvc.perform(get("/reports/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClaims").value(0))
                .andExpect(jsonPath("$.pendingClaims").value(0));

        verify(reportService, times(1)).generateClaimReport();
    }

    // ============================
    // Claim Report Tests (PDF Download)
    // ============================

    @Test
    void downloadClaimReport_success() throws Exception {
        byte[] pdfContent = "Mock PDF Content for Claims".getBytes();

        Mockito.when(reportService.generateClaimReportPdf()).thenReturn(pdfContent);

        mockMvc.perform(get("/reports/claims/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"claim_report.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(reportService, times(1)).generateClaimReportPdf();
    }

    @Test
    void downloadClaimReport_emptyPdf() throws Exception {
        byte[] emptyPdf = new byte[0];

        Mockito.when(reportService.generateClaimReportPdf()).thenReturn(emptyPdf);

        mockMvc.perform(get("/reports/claims/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(emptyPdf));

        verify(reportService, times(1)).generateClaimReportPdf();
    }

    // ============================
    // Policy Report Tests (JSON)
    // ============================

    @Test
    void generatePolicyReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 3);
        report.put("activePolicies", 2);
        report.put("expiredPolicies", 1);
        report.put("totalPremiumCollected", BigDecimal.valueOf(50000));

        Mockito.when(reportService.generatePolicyReport()).thenReturn(report);

        mockMvc.perform(get("/reports/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(3))
                .andExpect(jsonPath("$.activePolicies").value(2))
                .andExpect(jsonPath("$.expiredPolicies").value(1))
                .andExpect(jsonPath("$.totalPremiumCollected").value(50000));

        verify(reportService, times(1)).generatePolicyReport();
    }

    @Test
    void generatePolicyReport_emptyReport() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 0);
        report.put("activePolicies", 0);
        report.put("expiredPolicies", 0);
        report.put("totalPremiumCollected", BigDecimal.ZERO);

        Mockito.when(reportService.generatePolicyReport()).thenReturn(report);

        mockMvc.perform(get("/reports/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(0))
                .andExpect(jsonPath("$.totalPremiumCollected").value(0));

        verify(reportService, times(1)).generatePolicyReport();
    }

    // ============================
    // Policy Report Tests (PDF Download)
    // ============================

    @Test
    void downloadPolicyReport_success() throws Exception {
        byte[] pdfContent = "Mock PDF Content for Policies".getBytes();

        Mockito.when(reportService.generatePolicyReportPdf()).thenReturn(pdfContent);

        mockMvc.perform(get("/reports/policies/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"policy_report.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(reportService, times(1)).generatePolicyReportPdf();
    }

    @Test
    void downloadPolicyReport_emptyPdf() throws Exception {
        byte[] emptyPdf = new byte[0];

        Mockito.when(reportService.generatePolicyReportPdf()).thenReturn(emptyPdf);

        mockMvc.perform(get("/reports/policies/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(emptyPdf));

        verify(reportService, times(1)).generatePolicyReportPdf();
    }

    // ============================
    // Customer Report Tests (JSON)
    // ============================

    @Test
    void generateCustomerReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 2);
        report.put("totalClaims", 1);
        report.put("totalClaimAmount", BigDecimal.valueOf(5000));

        Mockito.when(reportService.generateCustomerReport(1)).thenReturn(report);

        mockMvc.perform(get("/reports/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(2))
                .andExpect(jsonPath("$.totalClaims").value(1))
                .andExpect(jsonPath("$.totalClaimAmount").value(5000));

        verify(reportService, times(1)).generateCustomerReport(1);
    }

    @Test
    void generateCustomerReport_differentCustomerId() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 5);
        report.put("totalClaims", 3);
        report.put("totalClaimAmount", BigDecimal.valueOf(15000));

        Mockito.when(reportService.generateCustomerReport(99)).thenReturn(report);

        mockMvc.perform(get("/reports/customer/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(5))
                .andExpect(jsonPath("$.totalClaims").value(3));

        verify(reportService, times(1)).generateCustomerReport(99);
    }

    @Test
    void generateCustomerReport_emptyReport() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("totalPolicies", 0);
        report.put("totalClaims", 0);
        report.put("totalClaimAmount", BigDecimal.ZERO);

        Mockito.when(reportService.generateCustomerReport(1)).thenReturn(report);

        mockMvc.perform(get("/reports/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(0))
                .andExpect(jsonPath("$.totalClaims").value(0))
                .andExpect(jsonPath("$.totalClaimAmount").value(0));

        verify(reportService, times(1)).generateCustomerReport(1);
    }

    // ============================
    // Customer Report Tests (PDF Download)
    // ============================

    @Test
    void downloadCustomerReport_success() throws Exception {
        byte[] pdfContent = "Mock PDF Content for Customer 1".getBytes();

        Mockito.when(reportService.generateCustomerReportPdf(1)).thenReturn(pdfContent);

        mockMvc.perform(get("/reports/customer/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"customer_1_report.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(reportService, times(1)).generateCustomerReportPdf(1);
    }

    @Test
    void downloadCustomerReport_differentCustomerId() throws Exception {
        byte[] pdfContent = "Mock PDF Content for Customer 99".getBytes();

        Mockito.when(reportService.generateCustomerReportPdf(99)).thenReturn(pdfContent);

        mockMvc.perform(get("/reports/customer/99/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"customer_99_report.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(reportService, times(1)).generateCustomerReportPdf(99);
    }

    @Test
    void downloadCustomerReport_emptyPdf() throws Exception {
        byte[] emptyPdf = new byte[0];

        Mockito.when(reportService.generateCustomerReportPdf(1)).thenReturn(emptyPdf);

        mockMvc.perform(get("/reports/customer/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(emptyPdf));

        verify(reportService, times(1)).generateCustomerReportPdf(1);
    }

    // ============================
    // Additional Edge Cases
    // ============================

    @Test
    void downloadCustomerReport_largeCustomerId() throws Exception {
        byte[] pdfContent = "PDF for large ID".getBytes();

        Mockito.when(reportService.generateCustomerReportPdf(999999)).thenReturn(pdfContent);

        mockMvc.perform(get("/reports/customer/999999/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"customer_999999_report.pdf\""));

        verify(reportService, times(1)).generateCustomerReportPdf(999999);
    }
}