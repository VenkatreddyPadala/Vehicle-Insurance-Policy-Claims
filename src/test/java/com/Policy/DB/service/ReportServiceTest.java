package com.Policy.DB.service;

import com.Policy.DB.model.*;
import com.Policy.DB.repository.ClaimRepository;
import com.Policy.DB.repository.PolicyRepository;
import com.Policy.DB.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ReportService reportService;

    // ---------------- Claim Report (JSON) ----------------
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
        assertEquals(BigDecimal.valueOf(2000), report.get("submittedClaimAmount"));
        assertEquals(1, report.get("pendingClaims"));
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

    // ---------------- Claim Report (PDF) ----------------
    @Test
    void generateClaimReportPdf_success() {
        // Arrange
        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setClaimAmount(BigDecimal.valueOf(1000));
        claim.setClaimDate(java.time.LocalDate.now()); // IMPORTANT
        claim.setClaimStatus(ClaimStatus.APPROVED);
        claim.setClaimReason("Accident damage");

        when(claimRepository.findAll()).thenReturn(List.of(claim));
        when(claimRepository.getTotalClaimAmountByStatus(any()))
                .thenReturn(BigDecimal.valueOf(1000));

        // Act
        byte[] pdf = reportService.generateClaimReportPdf();

        // Assert
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }


    // ---------------- Policy Report (JSON) ----------------
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
        assertEquals(1, report.get("expiredPolicies"));
        assertEquals(BigDecimal.valueOf(3000), report.get("totalPremiumCollected"));
    }

    // ---------------- Policy Report (PDF) ----------------
    @Test
    void generatePolicyReportPdf_success() {
        // Arrange
        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setPolicyNumber("POL123");
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setPremiumAmount(BigDecimal.valueOf(1500));
        policy.setStartDate(java.time.LocalDate.now()); // IMPORTANT
        policy.setEndDate(java.time.LocalDate.now().plusYears(1)); // IMPORTANT
        policy.setPolicyStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findAll()).thenReturn(List.of(policy));
        when(policyRepository.findByPolicyStatus(PolicyStatus.ACTIVE))
                .thenReturn(List.of(policy));
        when(policyRepository.findByPolicyStatus(PolicyStatus.EXPIRED))
                .thenReturn(List.of());

        // Act
        byte[] pdf = reportService.generatePolicyReportPdf();

        // Assert
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }


    // ---------------- Customer Report (JSON) ----------------
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

    // ---------------- Customer Report (PDF) ----------------
    @Test
    void generateCustomerReportPdf_success() {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("Venkat");
        customer.setEmail("venkat@test.com");
        customer.setPhone("9999999999");

        when(customerRepository.findById(1))
                .thenReturn(Optional.of(customer));
        when(policyRepository.findByCustomerId(1))
                .thenReturn(List.of());
        when(claimRepository.findByCustomerId(1))
                .thenReturn(List.of());

        byte[] pdf = reportService.generateCustomerReportPdf(1);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
    // ---------------- Claim Report (CSV) ----------------
    @Test
    void generateClaimReportCsv_success() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");

        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(customer);

        Policy policy = new Policy();
        policy.setPolicyNumber("POL123");
        policy.setVehicle(vehicle);

        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setClaimAmount(BigDecimal.valueOf(5000));
        claim.setClaimDate(java.time.LocalDate.now());
        claim.setClaimStatus(ClaimStatus.APPROVED);
        claim.setClaimReason("Accident damage");
        claim.setPolicy(policy);

        when(claimRepository.findAll()).thenReturn(List.of(claim));

        // Act
        byte[] csv = reportService.generateClaimReportCsv();

        // Assert
        assertNotNull(csv);
        assertTrue(csv.length > 0);

        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Claim ID"));
        assertTrue(csvContent.contains("POL123"));
        assertTrue(csvContent.contains("John Doe"));
        assertTrue(csvContent.contains("5000"));
        assertTrue(csvContent.contains("APPROVED"));
    }

    @Test
    void generateClaimReportCsv_emptyClaims() {
        // Arrange
        when(claimRepository.findAll()).thenReturn(List.of());

        // Act
        byte[] csv = reportService.generateClaimReportCsv();

        // Assert
        assertNotNull(csv);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Claim ID")); // Header should still be present
    }

    @Test
    void generateClaimReportCsv_nullPolicyData() {
        // Arrange
        Claim claim = new Claim();
        claim.setClaimId(1);
        claim.setClaimAmount(BigDecimal.valueOf(1000));
        claim.setClaimDate(java.time.LocalDate.now());
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        claim.setClaimReason("Test");
        claim.setPolicy(null); // Null policy

        when(claimRepository.findAll()).thenReturn(List.of(claim));

        // Act
        byte[] csv = reportService.generateClaimReportCsv();

        // Assert
        assertNotNull(csv);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("N/A")); // Should handle null policy
    }

    // ---------------- Policy Report (CSV) ----------------
    @Test
    void generatePolicyReportCsv_success() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("Jane Smith");

        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Honda");
        vehicle.setModel("Civic");
        vehicle.setCustomer(customer);

        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setPolicyNumber("POL456");
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setPremiumAmount(BigDecimal.valueOf(1500));
        policy.setStartDate(java.time.LocalDate.now());
        policy.setEndDate(java.time.LocalDate.now().plusYears(1));
        policy.setPolicyStatus(PolicyStatus.ACTIVE);
        policy.setVehicle(vehicle);

        when(policyRepository.findAll()).thenReturn(List.of(policy));

        // Act
        byte[] csv = reportService.generatePolicyReportCsv();

        // Assert
        assertNotNull(csv);
        assertTrue(csv.length > 0);

        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Policy ID"));
        assertTrue(csvContent.contains("POL456"));
        assertTrue(csvContent.contains("Jane Smith"));
        assertTrue(csvContent.contains("Honda Civic"));
        assertTrue(csvContent.contains("50000"));
        assertTrue(csvContent.contains("ACTIVE"));
    }

    @Test
    void generatePolicyReportCsv_emptyPolicies() {
        // Arrange
        when(policyRepository.findAll()).thenReturn(List.of());

        // Act
        byte[] csv = reportService.generatePolicyReportCsv();

        // Assert
        assertNotNull(csv);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Policy ID")); // Header should still be present
    }

    @Test
    void generatePolicyReportCsv_nullVehicleData() {
        // Arrange
        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setPolicyNumber("POL789");
        policy.setCoverageAmount(BigDecimal.valueOf(30000));
        policy.setPremiumAmount(BigDecimal.valueOf(1000));
        policy.setStartDate(java.time.LocalDate.now());
        policy.setEndDate(java.time.LocalDate.now().plusYears(1));
        policy.setPolicyStatus(PolicyStatus.EXPIRED);
        policy.setVehicle(null); // Null vehicle

        when(policyRepository.findAll()).thenReturn(List.of(policy));

        // Act
        byte[] csv = reportService.generatePolicyReportCsv();

        // Assert
        assertNotNull(csv);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("N/A")); // Should handle null vehicle
    }

    // ---------------- Customer Report (CSV) ----------------
    @Test
    void generateCustomerReportCsv_success() {
        // Arrange
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("Alice Brown");
        customer.setEmail("alice@test.com");
        customer.setPhone("8888888888");

        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Toyota");
        vehicle.setModel("Camry");

        Policy policy = new Policy();
        policy.setPolicyNumber("POL999");
        policy.setCoverageAmount(BigDecimal.valueOf(40000));
        policy.setPremiumAmount(BigDecimal.valueOf(1200));
        policy.setStartDate(java.time.LocalDate.now());
        policy.setEndDate(java.time.LocalDate.now().plusYears(1));
        policy.setPolicyStatus(PolicyStatus.ACTIVE);
        policy.setVehicle(vehicle);

        Claim claim = new Claim();
        claim.setClaimAmount(BigDecimal.valueOf(3000));
        claim.setClaimDate(java.time.LocalDate.now());
        claim.setClaimStatus(ClaimStatus.APPROVED);
        claim.setClaimReason("Minor damage");
        claim.setPolicy(policy);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(policyRepository.findByCustomerId(1)).thenReturn(List.of(policy));
        when(claimRepository.findByCustomerId(1)).thenReturn(List.of(claim));

        // Act
        byte[] csv = reportService.generateCustomerReportCsv(1);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.length > 0);

        String csvContent = new String(csv);
        assertTrue(csvContent.contains("CUSTOMER INFORMATION"));
        assertTrue(csvContent.contains("Alice Brown"));
        assertTrue(csvContent.contains("alice@test.com"));
        assertTrue(csvContent.contains("8888888888"));
        assertTrue(csvContent.contains("POLICIES"));
        assertTrue(csvContent.contains("POL999"));
        assertTrue(csvContent.contains("CLAIMS"));
        assertTrue(csvContent.contains("3000"));
    }

    @Test
    void generateCustomerReportCsv_noPoliciesNoClaims() {
        // Arrange
        Customer customer = new Customer();
        customer.setCustomerId(2);
        customer.setName("Bob Wilson");
        customer.setEmail("bob@test.com");
        customer.setPhone("7777777777");

        when(customerRepository.findById(2)).thenReturn(Optional.of(customer));
        when(policyRepository.findByCustomerId(2)).thenReturn(List.of());
        when(claimRepository.findByCustomerId(2)).thenReturn(List.of());

        // Act
        byte[] csv = reportService.generateCustomerReportCsv(2);

        // Assert
        assertNotNull(csv);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("Bob Wilson"));
        assertTrue(csvContent.contains("POLICIES"));
        assertTrue(csvContent.contains("CLAIMS"));
    }

    @Test
    void generateCustomerReportCsv_customerNotFound() {
        // Arrange
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportService.generateCustomerReportCsv(999);
        });
    }

    @Test
    void generateCustomerReportCsv_multiplePoliciesAndClaims() {
        // Arrange
        Customer customer = new Customer();
        customer.setCustomerId(3);
        customer.setName("Charlie Davis");
        customer.setEmail("charlie@test.com");
        customer.setPhone("6666666666");

        Policy policy1 = new Policy();
        policy1.setPolicyNumber("POL001");
        policy1.setCoverageAmount(BigDecimal.valueOf(50000));
        policy1.setPremiumAmount(BigDecimal.valueOf(1500));
        policy1.setStartDate(java.time.LocalDate.now());
        policy1.setEndDate(java.time.LocalDate.now().plusYears(1));
        policy1.setPolicyStatus(PolicyStatus.ACTIVE);

        Policy policy2 = new Policy();
        policy2.setPolicyNumber("POL002");
        policy2.setCoverageAmount(BigDecimal.valueOf(30000));
        policy2.setPremiumAmount(BigDecimal.valueOf(1000));
        policy2.setStartDate(java.time.LocalDate.now());
        policy2.setEndDate(java.time.LocalDate.now().plusYears(1));
        policy2.setPolicyStatus(PolicyStatus.EXPIRED);

        Claim claim1 = new Claim();
        claim1.setClaimAmount(BigDecimal.valueOf(2000));
        claim1.setClaimDate(java.time.LocalDate.now());
        claim1.setClaimStatus(ClaimStatus.APPROVED);
        claim1.setClaimReason("Claim 1");
        claim1.setPolicy(policy1);

        Claim claim2 = new Claim();
        claim2.setClaimAmount(BigDecimal.valueOf(1500));
        claim2.setClaimDate(java.time.LocalDate.now());
        claim2.setClaimStatus(ClaimStatus.SUBMITTED);
        claim2.setClaimReason("Claim 2");
        claim2.setPolicy(policy2);

        when(customerRepository.findById(3)).thenReturn(Optional.of(customer));
        when(policyRepository.findByCustomerId(3)).thenReturn(List.of(policy1, policy2));
        when(claimRepository.findByCustomerId(3)).thenReturn(List.of(claim1, claim2));

        // Act
        byte[] csv = reportService.generateCustomerReportCsv(3);

        // Assert
        assertNotNull(csv);
        String csvContent = new String(csv);
        assertTrue(csvContent.contains("POL001"));
        assertTrue(csvContent.contains("POL002"));
        assertTrue(csvContent.contains("2000"));
        assertTrue(csvContent.contains("1500"));
    }
}
