package com.Policy.DB.controller;

import com.Policy.DB.dto.*;
import com.Policy.DB.model.*;
import com.Policy.DB.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
class ApprovalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private ApprovalController approvalController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(approvalController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // Helper method to create a sample Customer
    private Customer createSampleCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("1234567890");
        customer.setAddress("123 Main St");
        return customer;
    }

    // Helper method to create a sample ApprovalRequest
    private ApprovalRequest createSampleApprovalRequest() {
        ApprovalRequest request = new ApprovalRequest();
        request.setRequestId(1);
        request.setCustomer(createSampleCustomer());
        request.setRequestType(RequestType.VEHICLE_REGISTRATION);
        request.setRequestData("{\"registrationNumber\":\"ABC123\"}");
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        return request;
    }

    // Helper method to create a sample ApprovalRequestDTO
    private ApprovalRequestDTO createSampleApprovalRequestDTO() {
        ApprovalRequestDTO dto = new ApprovalRequestDTO();
        dto.setRequestId(1);
        dto.setCustomerId(1);
        dto.setCustomerName("John Doe");
        dto.setCustomerEmail("john@example.com");
        dto.setRequestType(RequestType.VEHICLE_REGISTRATION);
        dto.setRequestData("{\"registrationNumber\":\"ABC123\"}");
        dto.setStatus(RequestStatus.PENDING);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    // Test: Submit Vehicle Request - Success
    @Test
    void testSubmitVehicleRequest_Success() throws Exception {
        // Arrange
        Integer customerId = 1;
        VehicleRequestDTO vehicleRequest = new VehicleRequestDTO();
        vehicleRequest.setRegistrationNumber("ABC123");
        vehicleRequest.setMake("Toyota");
        vehicleRequest.setModel("Camry");
        vehicleRequest.setYearOfManufacture(2020);
        vehicleRequest.setVehicleType(VehicleType.CAR);

        ApprovalRequest expectedRequest = createSampleApprovalRequest();

        when(approvalService.submitVehicleRequest(eq(customerId), org.mockito.ArgumentMatchers.any(VehicleRequestDTO.class)))
                .thenReturn(expectedRequest);

        // Act & Assert
        mockMvc.perform(post("/approvals/request/vehicle/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(approvalService, times(1)).submitVehicleRequest(eq(customerId), org.mockito.ArgumentMatchers.any(VehicleRequestDTO.class));
    }

    // Test: Submit Vehicle Request - Failure
    @Test
    void testSubmitVehicleRequest_Failure() throws Exception {
        // Arrange
        Integer customerId = 1;
        VehicleRequestDTO vehicleRequest = new VehicleRequestDTO();
        vehicleRequest.setRegistrationNumber("ABC123");

        when(approvalService.submitVehicleRequest(eq(customerId), org.mockito.ArgumentMatchers.any(VehicleRequestDTO.class)))
                .thenThrow(new RuntimeException("Customer not found"));

        // Act & Assert
        mockMvc.perform(post("/approvals/request/vehicle/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Customer not found"));

        verify(approvalService, times(1)).submitVehicleRequest(eq(customerId), org.mockito.ArgumentMatchers.any(VehicleRequestDTO.class));
    }

    // Test: Submit Policy Request - Success
    @Test
    void testSubmitPolicyRequest_Success() throws Exception {
        // Arrange
        Integer customerId = 1;
        PolicyRequestDTO policyRequest = new PolicyRequestDTO();
        policyRequest.setVehicleId(1);
        policyRequest.setCoverageAmount(new BigDecimal("50000"));
        policyRequest.setStartDate("2024-01-01");
        policyRequest.setEndDate("2025-01-01");

        ApprovalRequest expectedRequest = createSampleApprovalRequest();
        expectedRequest.setRequestType(RequestType.POLICY_CREATION);

        when(approvalService.submitPolicyRequest(eq(customerId), org.mockito.ArgumentMatchers.any(PolicyRequestDTO.class)))
                .thenReturn(expectedRequest);

        // Act & Assert
        mockMvc.perform(post("/approvals/request/policy/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.requestType").value("POLICY_CREATION"));

        verify(approvalService, times(1)).submitPolicyRequest(eq(customerId), org.mockito.ArgumentMatchers.any(PolicyRequestDTO.class));
    }

    // Test: Submit Policy Request - Vehicle Not Found
    @Test
    void testSubmitPolicyRequest_VehicleNotFound() throws Exception {
        // Arrange
        Integer customerId = 1;
        PolicyRequestDTO policyRequest = new PolicyRequestDTO();
        policyRequest.setVehicleId(999);
        policyRequest.setCoverageAmount(new BigDecimal("50000"));
        policyRequest.setStartDate("2024-01-01");
        policyRequest.setEndDate("2025-01-01");

        when(approvalService.submitPolicyRequest(eq(customerId), org.mockito.ArgumentMatchers.any(PolicyRequestDTO.class)))
                .thenThrow(new RuntimeException("Vehicle not found"));

        // Act & Assert
        mockMvc.perform(post("/approvals/request/policy/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vehicle not found"));

        verify(approvalService, times(1)).submitPolicyRequest(eq(customerId), org.mockito.ArgumentMatchers.any(PolicyRequestDTO.class));
    }

    // Test: Get Pending Requests
    @Test
    void testGetPendingRequests() throws Exception {
        // Arrange
        List<ApprovalRequestDTO> pendingRequests = Arrays.asList(
                createSampleApprovalRequestDTO(),
                createSampleApprovalRequestDTO()
        );

        when(approvalService.getPendingRequests()).thenReturn(pendingRequests);

        // Act & Assert
        mockMvc.perform(get("/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].requestId").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(approvalService, times(1)).getPendingRequests();
    }

    // Test: Get All Requests
    @Test
    void testGetAllRequests() throws Exception {
        // Arrange
        ApprovalRequestDTO request1 = createSampleApprovalRequestDTO();
        ApprovalRequestDTO request2 = createSampleApprovalRequestDTO();
        request2.setRequestId(2);
        request2.setStatus(RequestStatus.APPROVED);

        List<ApprovalRequestDTO> allRequests = Arrays.asList(request1, request2);

        when(approvalService.getAllRequests()).thenReturn(allRequests);

        // Act & Assert
        mockMvc.perform(get("/approvals/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));

        verify(approvalService, times(1)).getAllRequests();
    }

    // Test: Get Customer Requests
    @Test
    void testGetCustomerRequests() throws Exception {
        // Arrange
        Integer customerId = 1;
        List<ApprovalRequestDTO> customerRequests = Arrays.asList(
                createSampleApprovalRequestDTO()
        );

        when(approvalService.getCustomerRequests(customerId)).thenReturn(customerRequests);

        // Act & Assert
        mockMvc.perform(get("/approvals/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(approvalService, times(1)).getCustomerRequests(customerId);
    }

    // Test: Process Request - Approve
    @Test
    void testProcessRequest_Approve() throws Exception {
        // Arrange
        Integer requestId = 1;
        String adminUsername = "admin@example.com";

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);
        processData.setAdminComments("Approved");

        ApprovalRequest processedRequest = createSampleApprovalRequest();
        processedRequest.setStatus(RequestStatus.APPROVED);
        processedRequest.setAdminComments("Approved");
        processedRequest.setProcessedBy(adminUsername);
        processedRequest.setProcessedAt(LocalDateTime.now());

        when(approvalService.processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername)))
                .thenReturn(processedRequest);

        // Act & Assert
        mockMvc.perform(put("/approvals/process/{requestId}", requestId)
                        .header("X-Admin-Username", adminUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.adminComments").value("Approved"))
                .andExpect(jsonPath("$.processedBy").value(adminUsername));

        verify(approvalService, times(1)).processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername));
    }

    // Test: Process Request - Reject
    @Test
    void testProcessRequest_Reject() throws Exception {
        // Arrange
        Integer requestId = 1;
        String adminUsername = "admin@example.com";

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.REJECTED);
        processData.setAdminComments("Insufficient documentation");

        ApprovalRequest processedRequest = createSampleApprovalRequest();
        processedRequest.setStatus(RequestStatus.REJECTED);
        processedRequest.setAdminComments("Insufficient documentation");

        when(approvalService.processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername)))
                .thenReturn(processedRequest);

        // Act & Assert
        mockMvc.perform(put("/approvals/process/{requestId}", requestId)
                        .header("X-Admin-Username", adminUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(approvalService, times(1)).processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername));
    }

    // Test: Process Request - Request Not Found
    @Test
    void testProcessRequest_RequestNotFound() throws Exception {
        // Arrange
        Integer requestId = 999;
        String adminUsername = "admin@example.com";

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);

        when(approvalService.processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername)))
                .thenThrow(new RuntimeException("Request not found"));

        // Act & Assert
        mockMvc.perform(put("/approvals/process/{requestId}", requestId)
                        .header("X-Admin-Username", adminUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request not found"));

        verify(approvalService, times(1)).processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername));
    }

    // Test: Process Request - Already Processed
    @Test
    void testProcessRequest_AlreadyProcessed() throws Exception {
        // Arrange
        Integer requestId = 1;
        String adminUsername = "admin@example.com";

        ProcessRequestDTO processData = new ProcessRequestDTO();
        processData.setStatus(RequestStatus.APPROVED);

        when(approvalService.processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername)))
                .thenThrow(new RuntimeException("Request has already been processed"));

        // Act & Assert
        mockMvc.perform(put("/approvals/process/{requestId}", requestId)
                        .header("X-Admin-Username", adminUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request has already been processed"));

        verify(approvalService, times(1)).processRequest(eq(requestId), org.mockito.ArgumentMatchers.any(ProcessRequestDTO.class), eq(adminUsername));
    }

    // Test: Get Pending Count
    @Test
    void testGetPendingCount() throws Exception {
        // Arrange
        Long expectedCount = 5L;
        when(approvalService.getPendingRequestCount()).thenReturn(expectedCount);

        // Act & Assert
        mockMvc.perform(get("/approvals/pending/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));

        verify(approvalService, times(1)).getPendingRequestCount();
    }

    // Test: Get Pending Count - Zero
    @Test
    void testGetPendingCount_Zero() throws Exception {
        // Arrange
        when(approvalService.getPendingRequestCount()).thenReturn(0L);

        // Act & Assert
        mockMvc.perform(get("/approvals/pending/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        verify(approvalService, times(1)).getPendingRequestCount();
    }

    // Test: Submit Vehicle Request - Invalid Data
    @Test
    void testSubmitVehicleRequest_InvalidData() throws Exception {
        // Arrange
        Integer customerId = 1;
        VehicleRequestDTO vehicleRequest = new VehicleRequestDTO();
        // Missing required fields

        when(approvalService.submitVehicleRequest(eq(customerId), org.mockito.ArgumentMatchers.any(VehicleRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid vehicle data"));

        // Act & Assert
        mockMvc.perform(post("/approvals/request/vehicle/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid vehicle data"));
    }

    // Test: Submit Policy Request - Vehicle Doesn't Belong to Customer
    @Test
    void testSubmitPolicyRequest_VehicleDoesNotBelongToCustomer() throws Exception {
        // Arrange
        Integer customerId = 1;
        PolicyRequestDTO policyRequest = new PolicyRequestDTO();
        policyRequest.setVehicleId(2);
        policyRequest.setCoverageAmount(new BigDecimal("50000"));
        policyRequest.setStartDate("2024-01-01");
        policyRequest.setEndDate("2025-01-01");

        when(approvalService.submitPolicyRequest(eq(customerId), org.mockito.ArgumentMatchers.any(PolicyRequestDTO.class)))
                .thenThrow(new RuntimeException("Vehicle does not belong to this customer"));

        // Act & Assert
        mockMvc.perform(post("/approvals/request/policy/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vehicle does not belong to this customer"));
    }

    // Test: Get Customer Requests - Empty List
    @Test
    void testGetCustomerRequests_EmptyList() throws Exception {
        // Arrange
        Integer customerId = 1;
        when(approvalService.getCustomerRequests(customerId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/approvals/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(approvalService, times(1)).getCustomerRequests(customerId);
    }
}