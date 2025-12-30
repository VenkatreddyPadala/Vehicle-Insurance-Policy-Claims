package com.Policy.DB.service;

import com.Policy.DB.dto.*;
import com.Policy.DB.model.*;
import com.Policy.DB.repository.*;
import com.Policy.DB.util.PolicyNumberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApprovalService {

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyNumberGenerator policyNumberGenerator;

    @Autowired
    private PolicyService policyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Submit vehicle registration request
    public ApprovalRequest submitVehicleRequest(Integer customerId, VehicleRequestDTO vehicleRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        try {
            ApprovalRequest request = new ApprovalRequest();
            request.setCustomer(customer);
            request.setRequestType(RequestType.VEHICLE_REGISTRATION);
            request.setRequestData(objectMapper.writeValueAsString(vehicleRequest));
            request.setStatus(RequestStatus.PENDING);

            return approvalRequestRepository.save(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit vehicle request: " + e.getMessage());
        }
    }

    // Submit policy creation request
    public ApprovalRequest submitPolicyRequest(Integer customerId, PolicyRequestDTO policyRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Verify vehicle belongs to customer
        Vehicle vehicle = vehicleRepository.findById(policyRequest.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!vehicle.getCustomer().getCustomerId().equals(customerId)) {
            throw new RuntimeException("Vehicle does not belong to this customer");
        }

        try {
            ApprovalRequest request = new ApprovalRequest();
            request.setCustomer(customer);
            request.setRequestType(RequestType.POLICY_CREATION);
            request.setRequestData(objectMapper.writeValueAsString(policyRequest));
            request.setStatus(RequestStatus.PENDING);

            return approvalRequestRepository.save(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit policy request: " + e.getMessage());
        }
    }

    // Get all pending requests (for admin)
    public List<ApprovalRequestDTO> getPendingRequests() {
        return approvalRequestRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all requests (for admin)
    public List<ApprovalRequestDTO> getAllRequests() {
        return approvalRequestRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get customer's requests
    public List<ApprovalRequestDTO> getCustomerRequests(Integer customerId) {
        return approvalRequestRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Process request (approve/reject)
    public ApprovalRequest processRequest(Integer requestId, ProcessRequestDTO processData, String adminUsername) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }

        request.setStatus(processData.getStatus());
        request.setAdminComments(processData.getAdminComments());
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(adminUsername);

        // If approved, create the actual entity
        if (processData.getStatus() == RequestStatus.APPROVED) {
            try {
                if (request.getRequestType() == RequestType.VEHICLE_REGISTRATION) {
                    createVehicleFromRequest(request);
                } else if (request.getRequestType() == RequestType.POLICY_CREATION) {
                    createPolicyFromRequest(request);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create entity: " + e.getMessage());
            }
        }

        return approvalRequestRepository.save(request);
    }

    // Create vehicle from approved request
    private void createVehicleFromRequest(ApprovalRequest request) {
        try {
            VehicleRequestDTO vehicleData = objectMapper.readValue(
                    request.getRequestData(),
                    VehicleRequestDTO.class
            );

            Vehicle vehicle = new Vehicle();
            vehicle.setCustomer(request.getCustomer());
            vehicle.setRegistrationNumber(vehicleData.getRegistrationNumber());
            vehicle.setMake(vehicleData.getMake());
            vehicle.setModel(vehicleData.getModel());
            vehicle.setYearOfManufacture(vehicleData.getYearOfManufacture());
            vehicle.setVehicleType(vehicleData.getVehicleType());

            vehicleRepository.save(vehicle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create vehicle: " + e.getMessage());
        }
    }

    // Create policy from approved request
    private void createPolicyFromRequest(ApprovalRequest request) {
        try {
            PolicyRequestDTO policyData = objectMapper.readValue(
                    request.getRequestData(),
                    PolicyRequestDTO.class
            );

            Vehicle vehicle = vehicleRepository.findById(policyData.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));

            Policy policy = new Policy();
            policy.setVehicle(vehicle);

            // Generate unique policy number
            String policyNumber = policyNumberGenerator.generatePolicyNumberWithType(
                    vehicle.getVehicleType().toString()
            );
            policy.setPolicyNumber(policyNumber);

            policy.setCoverageAmount(policyData.getCoverageAmount());
            policy.setStartDate(policyData.getStartDate());
            policy.setEndDate(policyData.getEndDate());
            policy.setPolicyStatus(PolicyStatus.ACTIVE);

            // Calculate premium using existing logic
            policy.setPremiumAmount(policyService.calculatePremium(vehicle, policyData.getCoverageAmount()));

            policyRepository.save(policy);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create policy: " + e.getMessage());
        }
    }

    // Convert entity to DTO
    private ApprovalRequestDTO convertToDTO(ApprovalRequest request) {
        ApprovalRequestDTO dto = new ApprovalRequestDTO();
        dto.setRequestId(request.getRequestId());
        dto.setCustomerId(request.getCustomer().getCustomerId());
        dto.setCustomerName(request.getCustomer().getName());
        dto.setCustomerEmail(request.getCustomer().getEmail());
        dto.setRequestType(request.getRequestType());
        dto.setRequestData(request.getRequestData());
        dto.setStatus(request.getStatus());
        dto.setAdminComments(request.getAdminComments());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setProcessedAt(request.getProcessedAt());
        dto.setProcessedBy(request.getProcessedBy());
        return dto;
    }

    // Get pending request count
    public Long getPendingRequestCount() {
        return approvalRequestRepository.countByStatus(RequestStatus.PENDING);
    }
}