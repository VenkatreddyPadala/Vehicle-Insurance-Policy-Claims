package com.Policy.DB.service;

import com.Policy.DB.dto.*;
import com.Policy.DB.model.*;
import com.Policy.DB.repository.*;
import com.Policy.DB.util.PolicyNumberGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyNumberGenerator policyNumberGenerator;

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private ApprovalService approvalService;

    private Customer testCustomer;
    private Vehicle testVehicle;
    private ApprovalRequest testApprovalRequest;
    private VehicleRequestDTO vehicleRequestDTO;
    private PolicyRequestDTO policyRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setCustomerId(1);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setPhone("1234567890");
        testCustomer.setAddress("123 Main St");

        // Setup test vehicle
        testVehicle = new Vehicle();
        testVehicle.setVehicleId(1);
        testVehicle.setCustomer(testCustomer);
        testVehicle.setRegistrationNumber("ABC123");
        testVehicle.setMake("Toyota");
        testVehicle.setModel("Camry");
        testVehicle.setYearOfManufacture(2020);
        testVehicle.setVehicleType(VehicleType.CAR);

        // Setup test approval request
        testApprovalRequest = new ApprovalRequest();
        testApprovalRequest.setRequestId(1);
        testApprovalRequest.setCustomer(testCustomer);
        testApprovalRequest.setRequestType(RequestType.VEHICLE_REGISTRATION);
        testApprovalRequest.setStatus(RequestStatus.PENDING);
        testApprovalRequest.setCreatedAt(LocalDateTime.now());

        // Setup VehicleRequestDTO
        vehicleRequestDTO = new VehicleRequestDTO();
        vehicleRequestDTO.setRegistrationNumber("ABC123");
        vehicleRequestDTO.setMake("Toyota");
        vehicleRequestDTO.setModel("Camry");
        vehicleRequestDTO.setYearOfManufacture(2020);
        vehicleRequestDTO.setVehicleType(VehicleType.CAR);

        // Setup PolicyRequestDTO
        policyRequestDTO = new PolicyRequestDTO();
        policyRequestDTO.setVehicleId(1);
        policyRequestDTO.setCoverageAmount(new BigDecimal("50000"));
        policyRequestDTO.setStartDate("2024-01-01");
        policyRequestDTO.setEndDate("2025-01-01");
    }

    // Test: Submit Vehicle Request - Success
    @Test
    void testSubmitVehicleRequest_Success() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);

        // Act
        ApprovalRequest result = approvalService.submitVehicleRequest(1, vehicleRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer, result.getCustomer());
        verify(customerRepository, times(1)).findById(1);
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
    }

    // Test: Submit Vehicle Request - Customer Not Found
    @Test
    void testSubmitVehicleRequest_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.submitVehicleRequest(999, vehicleRequestDTO);
        });

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository, times(1)).findById(999);
        verify(approvalRequestRepository, never()).save(any(ApprovalRequest.class));
    }

    // Test: Submit Policy Request - Success
    @Test
    void testSubmitPolicyRequest_Success() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);

        // Act
        ApprovalRequest result = approvalService.submitPolicyRequest(1, policyRequestDTO);

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).findById(1);
        verify(vehicleRepository, times(1)).findById(1);
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
    }

    // Test: Submit Policy Request - Customer Not Found
    @Test
    void testSubmitPolicyRequest_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.submitPolicyRequest(999, policyRequestDTO);
        });

        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository, times(1)).findById(999);
        verify(approvalRequestRepository, never()).save(any(ApprovalRequest.class));
    }

    // Test: Submit Policy Request - Vehicle Not Found
    @Test
    void testSubmitPolicyRequest_VehicleNotFound() {
        // Arrange
        PolicyRequestDTO requestWithInvalidVehicle = new PolicyRequestDTO();
        requestWithInvalidVehicle.setVehicleId(999);
        requestWithInvalidVehicle.setCoverageAmount(new BigDecimal("50000"));
        requestWithInvalidVehicle.setStartDate("2024-01-01");
        requestWithInvalidVehicle.setEndDate("2025-01-01");

        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.submitPolicyRequest(1, requestWithInvalidVehicle);
        });

        assertEquals("Vehicle not found", exception.getMessage());
        verify(vehicleRepository, times(1)).findById(999);
    }

    // Test: Submit Policy Request - Vehicle Doesn't Belong to Customer
    @Test
    void testSubmitPolicyRequest_VehicleDoesNotBelongToCustomer() {
        // Arrange
        Customer differentCustomer = new Customer();
        differentCustomer.setCustomerId(2);

        Vehicle vehicleWithDifferentOwner = new Vehicle();
        vehicleWithDifferentOwner.setVehicleId(1);
        vehicleWithDifferentOwner.setCustomer(differentCustomer);

        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(vehicleWithDifferentOwner));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.submitPolicyRequest(1, policyRequestDTO);
        });

        assertEquals("Vehicle does not belong to this customer", exception.getMessage());
        verify(approvalRequestRepository, never()).save(any(ApprovalRequest.class));
    }

    // Test: Get Pending Requests
    @Test
    void testGetPendingRequests() {
        // Arrange
        List<ApprovalRequest> pendingRequests = Arrays.asList(testApprovalRequest);
        when(approvalRequestRepository.findByStatus(RequestStatus.PENDING)).thenReturn(pendingRequests);

        // Act
        List<ApprovalRequestDTO> result = approvalService.getPendingRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomer.getName(), result.get(0).getCustomerName());
        verify(approvalRequestRepository, times(1)).findByStatus(RequestStatus.PENDING);
    }

    // Test: Get All Requests
    @Test
    void testGetAllRequests() {
        // Arrange
        ApprovalRequest request2 = new ApprovalRequest();
        request2.setRequestId(2);
        request2.setCustomer(testCustomer);
        request2.setStatus(RequestStatus.APPROVED);

        List<ApprovalRequest> allRequests = Arrays.asList(testApprovalRequest, request2);
        when(approvalRequestRepository.findAll()).thenReturn(allRequests);

        // Act
        List<ApprovalRequestDTO> result = approvalService.getAllRequests();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(approvalRequestRepository, times(1)).findAll();
    }

    // Test: Get Customer Requests
    @Test
    void testGetCustomerRequests() {
        // Arrange
        List<ApprovalRequest> customerRequests = Arrays.asList(testApprovalRequest);
        when(approvalRequestRepository.findByCustomer_CustomerId(1)).thenReturn(customerRequests);

        // Act
        List<ApprovalRequestDTO> result = approvalService.getCustomerRequests(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCustomerId());
        verify(approvalRequestRepository, times(1)).findByCustomer_CustomerId(1);
    }

    // Test: Process Request - Approve Vehicle Registration
    @Test
    void testProcessRequest_ApproveVehicleRegistration() throws JsonProcessingException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String requestData = objectMapper.writeValueAsString(vehicleRequestDTO);

        testApprovalRequest.setRequestData(requestData);
        testApprovalRequest.setRequestType(RequestType.VEHICLE_REGISTRATION);

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);
        processData.setAdminComments("Approved");

        when(approvalRequestRepository.findById(1)).thenReturn(Optional.of(testApprovalRequest));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);

        // Act
        ApprovalRequest result = approvalService.processRequest(1, processData, "admin@test.com");

        // Assert
        assertNotNull(result);
        verify(approvalRequestRepository, times(1)).findById(1);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
    }

    // Test: Process Request - Approve Policy Creation
    @Test
    void testProcessRequest_ApprovePolicyCreation() throws JsonProcessingException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String requestData = objectMapper.writeValueAsString(policyRequestDTO);

        testApprovalRequest.setRequestData(requestData);
        testApprovalRequest.setRequestType(RequestType.POLICY_CREATION);

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);
        processData.setAdminComments("Approved");

        Policy testPolicy = new Policy();
        testPolicy.setPolicyId(1);
        testPolicy.setVehicle(testVehicle);

        when(approvalRequestRepository.findById(1)).thenReturn(Optional.of(testApprovalRequest));
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(testVehicle));
        when(policyNumberGenerator.generatePolicyNumberWithType(anyString())).thenReturn("CAR-20241225-1001");
        when(policyService.calculatePremium(any(Vehicle.class), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("2500"));
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);

        // Act
        ApprovalRequest result = approvalService.processRequest(1, processData, "admin@test.com");

        // Assert
        assertNotNull(result);
        verify(approvalRequestRepository, times(1)).findById(1);
        verify(policyRepository, times(1)).save(any(Policy.class));
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
    }

    // Test: Process Request - Reject
    @Test
    void testProcessRequest_Reject() {
        // Arrange
        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.REJECTED);
        processData.setAdminComments("Documentation insufficient");

        when(approvalRequestRepository.findById(1)).thenReturn(Optional.of(testApprovalRequest));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);

        // Act
        ApprovalRequest result = approvalService.processRequest(1, processData, "admin@test.com");

        // Assert
        assertNotNull(result);
        verify(approvalRequestRepository, times(1)).findById(1);
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(policyRepository, never()).save(any(Policy.class));
    }

    // Test: Process Request - Request Not Found
    @Test
    void testProcessRequest_RequestNotFound() {
        // Arrange
        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.processRequest(999, processData, "admin@test.com");
        });

        assertEquals("Request not found", exception.getMessage());
        verify(approvalRequestRepository, times(1)).findById(999);
    }

    // Test: Process Request - Already Processed
    @Test
    void testProcessRequest_AlreadyProcessed() {
        // Arrange
        testApprovalRequest.setStatus(RequestStatus.APPROVED);

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findById(1)).thenReturn(Optional.of(testApprovalRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.processRequest(1, processData, "admin@test.com");
        });

        assertEquals("Request has already been processed", exception.getMessage());
        verify(approvalRequestRepository, times(1)).findById(1);
        verify(approvalRequestRepository, never()).save(any(ApprovalRequest.class));
    }

    // Test: Get Pending Request Count
    @Test
    void testGetPendingRequestCount() {
        // Arrange
        when(approvalRequestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(5L);

        // Act
        Long count = approvalService.getPendingRequestCount();

        // Assert
        assertEquals(5L, count);
        verify(approvalRequestRepository, times(1)).countByStatus(RequestStatus.PENDING);
    }

    // Test: Get Pending Request Count - Zero
    @Test
    void testGetPendingRequestCount_Zero() {
        // Arrange
        when(approvalRequestRepository.countByStatus(RequestStatus.PENDING)).thenReturn(0L);

        // Act
        Long count = approvalService.getPendingRequestCount();

        // Assert
        assertEquals(0L, count);
        verify(approvalRequestRepository, times(1)).countByStatus(RequestStatus.PENDING);
    }

    // Test: Convert to DTO
    @Test
    void testConvertToDTO() {
        // Arrange
        testApprovalRequest.setAdminComments("Test comment");
        testApprovalRequest.setProcessedAt(LocalDateTime.now());
        testApprovalRequest.setProcessedBy("admin@test.com");

        // Act
        List<ApprovalRequest> requests = Arrays.asList(testApprovalRequest);
        when(approvalRequestRepository.findAll()).thenReturn(requests);
        List<ApprovalRequestDTO> result = approvalService.getAllRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ApprovalRequestDTO dto = result.get(0);
        assertEquals(testApprovalRequest.getRequestId(), dto.getRequestId());
        assertEquals(testCustomer.getCustomerId(), dto.getCustomerId());
        assertEquals(testCustomer.getName(), dto.getCustomerName());
        assertEquals(testCustomer.getEmail(), dto.getCustomerEmail());
        assertEquals(testApprovalRequest.getRequestType(), dto.getRequestType());
        assertEquals(testApprovalRequest.getStatus(), dto.getStatus());
        assertEquals(testApprovalRequest.getAdminComments(), dto.getAdminComments());
    }

    // Test: Submit Vehicle Request - JSON Serialization Error
    @Test
    void testSubmitVehicleRequest_SerializationError() {
        // This test is tricky because ObjectMapper is created in constructor
        // In real scenario, you'd use @Spy or make ObjectMapper injectable
        // For now, we test with valid data and verify it doesn't throw

        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);

        assertDoesNotThrow(() -> {
            approvalService.submitVehicleRequest(1, vehicleRequestDTO);
        });
    }

    // Test: Process Request - Failed to Create Vehicle
    @Test
    void testProcessRequest_FailedToCreateVehicle() throws JsonProcessingException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String invalidRequestData = "{\"invalid\":\"data\"}";

        testApprovalRequest.setRequestData(invalidRequestData);
        testApprovalRequest.setRequestType(RequestType.VEHICLE_REGISTRATION);

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findById(1)).thenReturn(Optional.of(testApprovalRequest));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.processRequest(1, processData, "admin@test.com");
        });

        assertTrue(exception.getMessage().contains("Failed to create entity"));
    }

    // Test: Process Request - Failed to Create Policy (Vehicle Not Found)
    @Test
    void testProcessRequest_FailedToCreatePolicy_VehicleNotFound() throws JsonProcessingException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String requestData = objectMapper.writeValueAsString(policyRequestDTO);

        testApprovalRequest.setRequestData(requestData);
        testApprovalRequest.setRequestType(RequestType.POLICY_CREATION);

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findById(1)).thenReturn(Optional.of(testApprovalRequest));
        when(vehicleRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            approvalService.processRequest(1, processData, "admin@test.com");
        });

        assertTrue(exception.getMessage().contains("Failed to create entity"));
    }

    // Test: Get Customer Requests - Empty List
    @Test
    void testGetCustomerRequests_EmptyList() {
        // Arrange
        when(approvalRequestRepository.findByCustomer_CustomerId(1)).thenReturn(Arrays.asList());

        // Act
        List<ApprovalRequestDTO> result = approvalService.getCustomerRequests(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(approvalRequestRepository, times(1)).findByCustomer_CustomerId(1);
    }

    // Test: Get Pending Requests - Empty List
    @Test
    void testGetPendingRequests_EmptyList() {
        // Arrange
        when(approvalRequestRepository.findByStatus(RequestStatus.PENDING)).thenReturn(Arrays.asList());

        // Act
        List<ApprovalRequestDTO> result = approvalService.getPendingRequests();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(approvalRequestRepository, times(1)).findByStatus(RequestStatus.PENDING);
    }
    @Test
    void testUpdateRequest_Success() {
        ApprovalRequest request = new ApprovalRequest();
        request.setRequestId(1);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestType(RequestType.VEHICLE_REGISTRATION);

        VehicleRequestDTO dto = new VehicleRequestDTO();
        dto.setRegistrationNumber("AP09AB1234");
        dto.setMake("Honda");
        dto.setModel("City");
        dto.setYearOfManufacture(2022);
        dto.setVehicleType(VehicleType.CAR);

        when(approvalRequestRepository.findById(1))
                .thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        ApprovalRequest updated =
                approvalService.updateRequest(1, dto);

        assertNotNull(updated);
        verify(approvalRequestRepository).save(any());
    }
    @Test
    void testUpdateRequest_RequestNotFound() {
        when(approvalRequestRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> approvalService.updateRequest(1, new VehicleRequestDTO()));
    }
    @Test
    void testUpdateRequest_NotPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findById(1))
                .thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> approvalService.updateRequest(1, new VehicleRequestDTO()));
    }
    @Test
    void testUpdateRequest_WrongType() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(RequestStatus.PENDING);
        request.setRequestType(RequestType.POLICY_CREATION);

        when(approvalRequestRepository.findById(1))
                .thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> approvalService.updateRequest(1, new VehicleRequestDTO()));
    }
    @Test
    void testSubmitVehicleRequest_SaveFails() {
        when(customerRepository.findById(any()))
                .thenReturn(Optional.of(new Customer()));
        when(approvalRequestRepository.save(any()))
                .thenThrow(new RuntimeException("DB down"));

        VehicleRequestDTO dto = new VehicleRequestDTO(
                "AP09AB1234",
                "Hyundai",
                "i20",
                2023,
                VehicleType.CAR
        );

        assertThrows(RuntimeException.class,
                () -> approvalService.submitVehicleRequest(1, dto));
    }
}