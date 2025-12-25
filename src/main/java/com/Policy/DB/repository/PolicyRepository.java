package com.Policy.DB.repository;
import com.Policy.DB.model.Policy;
import com.Policy.DB.model.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    // Find all policies for a vehicle
    List<Policy> findByVehicle_VehicleId(Integer vehicleId);

    // Find by policy number
    Optional<Policy> findByPolicyNumber(String policyNumber);

    // Find by status
    List<Policy> findByPolicyStatus(PolicyStatus status);

    // Find active policies
    @Query("SELECT p FROM Policy p WHERE p.policyStatus = 'ACTIVE' AND p.endDate >= :currentDate")
    List<Policy> findActivePolicies(LocalDate currentDate);

    // Find expired policies
    @Query("SELECT p FROM Policy p WHERE p.endDate < :currentDate")
    List<Policy> findExpiredPolicies(LocalDate currentDate);

    // Find policies expiring soon (within next 30 days)
    @Query("SELECT p FROM Policy p WHERE p.policyStatus = 'ACTIVE' AND p.endDate BETWEEN :startDate AND :endDate")
    List<Policy> findPoliciesExpiringSoon(LocalDate startDate, LocalDate endDate);

    // Find all policies for a customer
    @Query("SELECT p FROM Policy p WHERE p.vehicle.customer.customerId = :customerId")
    List<Policy> findByCustomerId(Integer customerId);

    boolean existsByPolicyNumber(String policyNumber);
}
