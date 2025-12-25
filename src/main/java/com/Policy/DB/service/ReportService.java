package com.Policy.DB.service;
import com.Policy.DB.model.Claim;
import com.Policy.DB.model.Policy;
import com.Policy.DB.model.ClaimStatus;
import com.Policy.DB.repository.ClaimRepository;
import com.Policy.DB.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Transactional(readOnly = true)
public class ReportService {
    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;
    // Generate claim report
    public Map<String, Object> generateClaimReport() {
        Map<String, Object> report = new HashMap<>();

        List<Claim> allClaims = claimRepository.findAll();
        report.put("totalClaims", allClaims.size());

        BigDecimal approvedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.APPROVED);
        BigDecimal rejectedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.REJECTED);
        BigDecimal submittedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.SUBMITTED);

        report.put("approvedClaimAmount", approvedAmount != null ? approvedAmount : BigDecimal.ZERO);
        report.put("rejectedClaimAmount", rejectedAmount != null ? rejectedAmount : BigDecimal.ZERO);
        report.put("submittedClaimAmount", submittedAmount != null ? submittedAmount : BigDecimal.ZERO);

        List<Claim> pendingClaims = claimRepository.findPendingClaims();
        report.put("pendingClaims", pendingClaims.size());

        return report;
    }

    // Generate policy report
    public Map<String, Object> generatePolicyReport() {
        Map<String, Object> report = new HashMap<>();

        List<Policy> allPolicies = policyRepository.findAll();
        report.put("totalPolicies", allPolicies.size());

        List<Policy> activePolicies = policyRepository.findByPolicyStatus(com.Policy.DB.model.PolicyStatus.ACTIVE);
        report.put("activePolicies", activePolicies.size());

        List<Policy> expiredPolicies = policyRepository.findByPolicyStatus(com.Policy.DB.model.PolicyStatus.EXPIRED);
        report.put("expiredPolicies", expiredPolicies.size());

        // Calculate total premium collected
        BigDecimal totalPremium = allPolicies.stream()
                .map(Policy::getPremiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalPremiumCollected", totalPremium);

        return report;
    }

    // Generate customer report
    public Map<String, Object> generateCustomerReport(Integer customerId) {
        Map<String, Object> report = new HashMap<>();

        List<Policy> customerPolicies = policyRepository.findByCustomerId(customerId);
        report.put("totalPolicies", customerPolicies.size());

        List<Claim> customerClaims = claimRepository.findByCustomerId(customerId);
        report.put("totalClaims", customerClaims.size());

        BigDecimal totalClaimAmount = customerClaims.stream()
                .map(Claim::getClaimAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalClaimAmount", totalClaimAmount);

        return report;
    }
}
