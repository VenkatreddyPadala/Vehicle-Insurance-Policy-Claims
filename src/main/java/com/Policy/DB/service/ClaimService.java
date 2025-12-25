package com.Policy.DB.service;
import com.Policy.DB.model.Policy;
import com.Policy.DB.model.Claim;
import com.Policy.DB.model.ClaimStatus;
import com.Policy.DB.repository.ClaimRepository;
import com.Policy.DB.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
@Transactional
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private PolicyRepository policyRepository;

    // File a new claim
    public Claim fileClaim(Claim claim, Integer policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        // Validate policy is active
        if (policy.getPolicyStatus().equals(com.Policy.DB.model.PolicyStatus.EXPIRED)) {
            throw new RuntimeException("Cannot file claim for expired policy");
        }

        // Validate claim amount doesn't exceed coverage
        if (claim.getClaimAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new RuntimeException("Claim amount exceeds coverage amount");
        }

        claim.setPolicy(policy);
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        return claimRepository.save(claim);
    }
    // Get claim status
    public Claim getClaimStatus(Integer claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
    }
    // Process claim (approve or reject)
    public Claim processClaim(Integer claimId, ClaimStatus newStatus) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));

        if (!claim.getClaimStatus().equals(ClaimStatus.SUBMITTED)) {
            throw new RuntimeException("Claim has already been processed");
        }

        if (newStatus != ClaimStatus.APPROVED && newStatus != ClaimStatus.REJECTED) {
            throw new RuntimeException("Invalid claim status. Must be APPROVED or REJECTED");
        }

        claim.setClaimStatus(newStatus);
        return claimRepository.save(claim);
    }
    // Get all claims
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    // Get claims by policy
    public List<Claim> getClaimsByPolicyId(Integer policyId) {
        return claimRepository.findByPolicy_PolicyId(policyId);
    }

    // Get claims by customer
    public List<Claim> getClaimsByCustomerId(Integer customerId) {
        return claimRepository.findByCustomerId(customerId);
    }

    // Get pending claims
    public List<Claim> getPendingClaims() {
        return claimRepository.findPendingClaims();
    }

    // Get claims by status
    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByClaimStatus(status);
    }
}
