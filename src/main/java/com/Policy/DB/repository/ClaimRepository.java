package com.Policy.DB.repository;

import com.Policy.DB.model.Claim;
import com.Policy.DB.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim,Integer> {
    // Find all claims for a policy
    List<Claim> findByPolicy_PolicyId(Integer policyId);

    // Find by claim status
    List<Claim> findByClaimStatus(ClaimStatus status);

    // Find claims by date range
    List<Claim> findByClaimDateBetween(LocalDate startDate, LocalDate endDate);

    // Find claims greater than amount
    List<Claim> findByClaimAmountGreaterThan(BigDecimal amount);

    // Find all claims for a customer
    @Query("SELECT c FROM Claim c WHERE c.policy.vehicle.customer.customerId = :customerId")
    List<Claim> findByCustomerId(Integer customerId);

    // Get total claim amount by status
    @Query("SELECT SUM(c.claimAmount) FROM Claim c WHERE c.claimStatus = :status")
    BigDecimal getTotalClaimAmountByStatus(ClaimStatus status);

    // Get pending claims (submitted but not approved/rejected)
    @Query("SELECT c FROM Claim c WHERE c.claimStatus = 'SUBMITTED' ORDER BY c.claimDate ASC")
    List<Claim> findPendingClaims();

    // Count claims by policy
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.policy.policyId = :policyId")
    Long countClaimsByPolicyId(Integer policyId);
}
