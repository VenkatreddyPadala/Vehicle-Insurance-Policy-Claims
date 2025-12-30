package com.Policy.DB.controller;
import com.Policy.DB.dto.*;
import com.Policy.DB.model.ApprovalRequest;
import com.Policy.DB.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/approvals")
@CrossOrigin(origins = "*")
public class ApprovalController {
    @Autowired
    private ApprovalService approvalService;

    // Customer submits vehicle registration request
    @PostMapping("/request/vehicle/{customerId}")
    public ResponseEntity<?> submitVehicleRequest(
            @PathVariable Integer customerId,
            @RequestBody VehicleRequestDTO vehicleRequest) {
        try {
            ApprovalRequest request = approvalService.submitVehicleRequest(customerId, vehicleRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Customer submits policy creation request
    @PostMapping("/request/policy/{customerId}")
    public ResponseEntity<?> submitPolicyRequest(
            @PathVariable Integer customerId,
            @RequestBody PolicyRequestDTO policyRequest) {
        try {
            ApprovalRequest request = approvalService.submitPolicyRequest(customerId, policyRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Get all pending requests (Admin)
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalRequestDTO>> getPendingRequests() {
        List<ApprovalRequestDTO> requests = approvalService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    // Get all requests (Admin)
    @GetMapping("/all")
    public ResponseEntity<List<ApprovalRequestDTO>> getAllRequests() {
        List<ApprovalRequestDTO> requests = approvalService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    // Get customer's requests
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ApprovalRequestDTO>> getCustomerRequests(@PathVariable Integer customerId) {
        List<ApprovalRequestDTO> requests = approvalService.getCustomerRequests(customerId);
        return ResponseEntity.ok(requests);
    }

    // Process request (Admin - Approve/Reject)
    @PutMapping("/process/{requestId}")
    public ResponseEntity<?> processRequest(
            @PathVariable Integer requestId,
            @RequestBody ProcessRequestDTO processData,
            @RequestHeader("X-Admin-Username") String adminUsername) {
        try {
            ApprovalRequest processed = approvalService.processRequest(requestId, processData, adminUsername);
            return ResponseEntity.ok(processed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Get pending request count
    @GetMapping("/pending/count")
    public ResponseEntity<CountResponse> getPendingCount() {
        Long count = approvalService.getPendingRequestCount();
        return ResponseEntity.ok(new CountResponse(count));
    }

    // Inner classes for responses
    static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    static class CountResponse {
        public Long count;
        public CountResponse(Long count) {
            this.count = count;
        }
    }
}
