package com.Policy.DB.service;

import com.Policy.DB.model.*;
import com.Policy.DB.repository.PolicyRepository;
import com.Policy.DB.repository.VehicleRepository;
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
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private PolicyService policyService;

    // -------- Create Policy --------
    @Test
    void createPolicy_success() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(VehicleType.CAR);
        vehicle.setYearOfManufacture(2018);

        Policy policy = new Policy();
        policy.setPolicyNumber("POL123");
        policy.setCoverageAmount(new BigDecimal("500000"));

        when(vehicleRepository.findById(1)).thenReturn(Optional.of(vehicle));
        when(policyRepository.existsByPolicyNumber("POL123")).thenReturn(false);
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        Policy result = policyService.createPolicy(policy, 1);

        assertNotNull(result);
        assertEquals(PolicyStatus.ACTIVE, result.getPolicyStatus());
        verify(policyRepository).save(policy);
    }

    // -------- Create Policy - Vehicle Not Found --------
    @Test
    void createPolicy_vehicleNotFound() {
        when(vehicleRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> policyService.createPolicy(new Policy(), 99));
    }

    // -------- Get Policy Details --------
    @Test
    void getPolicyDetails_success() {
        Policy policy = new Policy();
        policy.setPolicyId(1);

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        Policy result = policyService.getPolicyDetails(1);

        assertEquals(1, result.getPolicyId());
    }

    // -------- Renew Policy --------
    @Test
    void renewPolicy_success() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleType(VehicleType.BIKE);
        vehicle.setYearOfManufacture(2015);

        Policy policy = new Policy();
        policy.setVehicle(vehicle);
        policy.setCoverageAmount(new BigDecimal("200000"));

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        Policy result = policyService.renewPolicy(1, LocalDate.now().plusYears(1));

        assertEquals(PolicyStatus.ACTIVE, result.getPolicyStatus());
        assertNotNull(result.getPremiumAmount());
    }

    // -------- Get Active Policies --------
    @Test
    void getActivePolicies_success() {
        when(policyRepository.findActivePolicies(any(LocalDate.class)))
                .thenReturn(List.of(new Policy()));

        List<Policy> result = policyService.getActivePolicies();

        assertEquals(1, result.size());
    }

    // -------- Get Expired Policies --------
    @Test
    void getExpiredPolicies_success() {
        when(policyRepository.findExpiredPolicies(any(LocalDate.class)))
                .thenReturn(List.of(new Policy()));

        List<Policy> result = policyService.getExpiredPolicies();

        assertEquals(1, result.size());
    }
}
