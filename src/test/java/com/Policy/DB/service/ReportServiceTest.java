package com.Policy.DB.service;

import com.Policy.DB.model.Claim;
import com.Policy.DB.model.ClaimStatus;
import com.Policy.DB.model.Policy;
import com.Policy.DB.model.PolicyStatus;
import com.Policy.DB.repository.ClaimRepository;
import com.Policy.DB.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ReportService reportService;

    // -------- Claim Report --------
    @Test
    void generateClaimReport_success() {
        when(claimRepository.findAll()).thenReturn(List.of(new Claim(), new Claim()));
        when(claimRepository.getTotalClaimAmountByStatus(ClaimStatus.APPROVED))
                .thenReturn(BigDecimal.valueOf(5000));
        when(claimRepository.getTotalClaimAmountByStatus(ClaimStatus.REJECTED))
                .thenReturn(null);
        when(claimRepository.getTotalClaimAmountByStatus(ClaimStatus.SUBMITTED))
                .thenReturn(BigDecimal.valueOf(2000));
        when(claimRepository.findPendingClaims())
                .thenReturn(List.of(new Claim()));

        Map<String, Object> report = reportService.generateClaimReport();

        assertEquals(2, report.get("totalClaims"));
        assertEquals(BigDecimal.valueOf(5000), report.get("approvedClaimAmount"));
        assertEquals(BigDecimal.ZERO, report.get("rejectedClaimAmount"));
        assertEquals(1, report.get("pendingClaims"));
    }

    // -------- Policy Report --------
    @Test
    void generatePolicyReport_success() {
        Policy p1 = new Policy();
        p1.setPremiumAmount(BigDecimal.valueOf(1000));

        Policy p2 = new Policy();
        p2.setPremiumAmount(BigDecimal.valueOf(2000));

        when(policyRepository.findAll()).thenReturn(List.of(p1, p2));
        when(policyRepository.findByPolicyStatus(PolicyStatus.ACTIVE))
                .thenReturn(List.of(p1));
        when(policyRepository.findByPolicyStatus(PolicyStatus.EXPIRED))
                .thenReturn(List.of(p2));

        Map<String, Object> report = reportService.generatePolicyReport();

        assertEquals(2, report.get("totalPolicies"));
        assertEquals(1, report.get("activePolicies"));
        assertEquals(BigDecimal.valueOf(3000), report.get("totalPremiumCollected"));
    }

    // -------- Customer Report --------
    @Test
    void generateCustomerReport_success() {
        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(5000));

        Policy policy = new Policy();

        when(policyRepository.findByCustomerId(1))
                .thenReturn(List.of(policy));
        when(claimRepository.findByCustomerId(1))
                .thenReturn(List.of(claim));

        Map<String, Object> report = reportService.generateCustomerReport(1);

        assertEquals(1, report.get("totalPolicies"));
        assertEquals(1, report.get("totalClaims"));
        assertEquals(BigDecimal.valueOf(5000), report.get("totalClaimAmount"));
    }
    @Test
    void generateClaimReport_submittedAmountNull() {
        when(claimRepository.findAll()).thenReturn(List.of(new Claim()));
        when(claimRepository.getTotalClaimAmountByStatus(ClaimStatus.APPROVED))
                .thenReturn(BigDecimal.valueOf(1000));
        when(claimRepository.getTotalClaimAmountByStatus(ClaimStatus.REJECTED))
                .thenReturn(BigDecimal.valueOf(500));
        when(claimRepository.getTotalClaimAmountByStatus(ClaimStatus.SUBMITTED))
                .thenReturn(null);
        when(claimRepository.findPendingClaims())
                .thenReturn(List.of());

        Map<String, Object> report = reportService.generateClaimReport();

        assertEquals(BigDecimal.ZERO, report.get("submittedClaimAmount"));
    }
    @Test
    void generateClaimReport_allAmountsNull() {
        when(claimRepository.findAll()).thenReturn(List.of());
        when(claimRepository.getTotalClaimAmountByStatus(any()))
                .thenReturn(null);
        when(claimRepository.findPendingClaims())
                .thenReturn(List.of());

        Map<String, Object> report = reportService.generateClaimReport();

        assertEquals(BigDecimal.ZERO, report.get("approvedClaimAmount"));
        assertEquals(BigDecimal.ZERO, report.get("rejectedClaimAmount"));
        assertEquals(BigDecimal.ZERO, report.get("submittedClaimAmount"));
    }

}
