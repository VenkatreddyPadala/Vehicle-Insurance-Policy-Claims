package com.Policy.DB.service;

import com.Policy.DB.model.*;
import com.Policy.DB.repository.ClaimRepository;
import com.Policy.DB.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ClaimService claimService;

    @Test
    void fileClaim_success() {
        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setPolicyStatus(PolicyStatus.ACTIVE);
        policy.setCoverageAmount(BigDecimal.valueOf(50000));

        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(10000));
        claim.setClaimDate(LocalDate.now());

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        Claim savedClaim = claimService.fileClaim(claim, 1);

        assertNotNull(savedClaim);
        verify(claimRepository).save(claim);
    }

    @Test
    void getClaimStatus_success() {
        Claim claim = new Claim();
        claim.setClaimId(1);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        Claim result = claimService.getClaimStatus(1);

        assertEquals(1, result.getClaimId());
    }

    @Test
    void processClaim_approved() {
        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(claim)).thenReturn(claim);

        Claim processed = claimService.processClaim(1, ClaimStatus.APPROVED);

        assertEquals(ClaimStatus.APPROVED, processed.getClaimStatus());
    }

    @Test
    void getAllClaims_success() {
        when(claimRepository.findAll()).thenReturn(List.of(new Claim()));

        List<Claim> claims = claimService.getAllClaims();

        assertEquals(1, claims.size());
    }

    @Test
    void getClaimsByPolicyId_success() {
        when(claimRepository.findByPolicy_PolicyId(1))
                .thenReturn(List.of(new Claim()));

        List<Claim> claims = claimService.getClaimsByPolicyId(1);

        assertFalse(claims.isEmpty());
    }

    @Test
    void getClaimsByCustomerId_success() {
        when(claimRepository.findByCustomerId(1))
                .thenReturn(List.of(new Claim()));

        List<Claim> claims = claimService.getClaimsByCustomerId(1);

        assertEquals(1, claims.size());
    }

    @Test
    void getPendingClaims_success() {
        when(claimRepository.findPendingClaims())
                .thenReturn(List.of(new Claim()));

        List<Claim> claims = claimService.getPendingClaims();

        assertEquals(1, claims.size());
    }

    @Test
    void getClaimsByStatus_success() {
        when(claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED))
                .thenReturn(List.of(new Claim()));

        List<Claim> claims = claimService.getClaimsByStatus(ClaimStatus.SUBMITTED);

        assertEquals(1, claims.size());
    }
    @Test
    void fileClaim_policyExpired() {
        Policy policy = new Policy();
        policy.setPolicyStatus(PolicyStatus.EXPIRED);
        policy.setCoverageAmount(BigDecimal.valueOf(50000));

        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(10000));

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.fileClaim(claim, 1));

        assertEquals("Cannot file claim for expired policy", ex.getMessage());
    }

    @Test
    void fileClaim_amountExceedsCoverage() {
        Policy policy = new Policy();
        policy.setPolicyStatus(PolicyStatus.ACTIVE);
        policy.setCoverageAmount(BigDecimal.valueOf(10000));

        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(50000));

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.fileClaim(claim, 1));

        assertEquals("Claim amount exceeds coverage amount", ex.getMessage());
    }
    @Test
    void fileClaim_policyNotFound() {
        when(policyRepository.findById(1)).thenReturn(Optional.empty());

        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(1000));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.fileClaim(claim, 1));

        assertTrue(ex.getMessage().contains("Policy not found"));
    }
    @Test
    void processClaim_alreadyProcessed() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.APPROVED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.processClaim(1, ClaimStatus.REJECTED));

        assertEquals("Claim has already been processed", ex.getMessage());
    }
    @Test
    void processClaim_invalidStatus() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.processClaim(1, ClaimStatus.SUBMITTED));

        assertEquals("Invalid claim status. Must be APPROVED or REJECTED", ex.getMessage());
    }
    @Test
    void processClaim_claimNotFound() {
        when(claimRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.processClaim(1, ClaimStatus.APPROVED));

        assertTrue(ex.getMessage().contains("Claim not found"));
    }
    @Test
    void getClaimStatus_notFound() {
        when(claimRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.getClaimStatus(1));

        assertTrue(ex.getMessage().contains("Claim not found"));
    }

}
