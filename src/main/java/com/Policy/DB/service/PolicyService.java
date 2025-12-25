package com.Policy.DB.service;
import com.Policy.DB.model.Vehicle;
import com.Policy.DB.model.Policy;
import com.Policy.DB.model.PolicyStatus;
import com.Policy.DB.repository.PolicyRepository;
import com.Policy.DB.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
@Service
@Transactional
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // Create new policy
    public Policy createPolicy(Policy policy, Integer vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        if (policyRepository.existsByPolicyNumber(policy.getPolicyNumber())) {
            throw new RuntimeException("Policy with this number already exists");
        }

        policy.setVehicle(vehicle);

        // Calculate premium based on vehicle type and age
        BigDecimal calculatedPremium = calculatePremium(vehicle, policy.getCoverageAmount());
        policy.setPremiumAmount(calculatedPremium);

        policy.setPolicyStatus(PolicyStatus.ACTIVE);

        return policyRepository.save(policy);
    }

    // Calculate premium based on vehicle type, age, and coverage
    private BigDecimal calculatePremium(Vehicle vehicle, BigDecimal coverageAmount) {
        BigDecimal basePremium = BigDecimal.ZERO;

        // Base premium by vehicle type
        switch (vehicle.getVehicleType()) {
            case CAR:
                basePremium = new BigDecimal("1500");
                break;
            case BIKE:
                basePremium = new BigDecimal("800");
                break;
            case TRUCK:
                basePremium = new BigDecimal("2500");
                break;
        }

        // Adjust premium based on vehicle age
        int vehicleAge = LocalDate.now().getYear() - vehicle.getYearOfManufacture();
        if (vehicleAge > 10) {
            basePremium = basePremium.multiply(new BigDecimal("1.5"));
        } else if (vehicleAge > 5) {
            basePremium = basePremium.multiply(new BigDecimal("1.2"));
        }

        // Adjust based on coverage amount (2% of coverage amount)
        BigDecimal coverageFactor = coverageAmount.multiply(new BigDecimal("0.02"));

        return basePremium.add(coverageFactor);
    }

    // Get policy details
    public Policy getPolicyDetails(Integer policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));
    }

    // Renew policy
    public Policy renewPolicy(Integer policyId, LocalDate newEndDate) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        policy.setStartDate(LocalDate.now());
        policy.setEndDate(newEndDate);
        policy.setPolicyStatus(PolicyStatus.ACTIVE);

        // Recalculate premium for renewal
        BigDecimal newPremium = calculatePremium(policy.getVehicle(), policy.getCoverageAmount());
        policy.setPremiumAmount(newPremium);

        return policyRepository.save(policy);
    }

    // Get all policies
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    // Get policies by customer
    public List<Policy> getPoliciesByCustomerId(Integer customerId) {
        return policyRepository.findByCustomerId(customerId);
    }

    // Get active policies
    public List<Policy> getActivePolicies() {
        return policyRepository.findActivePolicies(LocalDate.now());
    }

    // Get expired policies
    public List<Policy> getExpiredPolicies() {
        return policyRepository.findExpiredPolicies(LocalDate.now());
    }

    // Update policy status (check if expired)
    public void updatePolicyStatus(Integer policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with id: " + policyId));

        if (policy.getEndDate().isBefore(LocalDate.now())) {
            policy.setPolicyStatus(PolicyStatus.EXPIRED);
            policyRepository.save(policy);
        }
    }

}
