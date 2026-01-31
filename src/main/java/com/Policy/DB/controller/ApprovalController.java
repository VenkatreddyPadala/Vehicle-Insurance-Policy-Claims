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

    // UPDATE: Customer updates vehicle registration request (only if PENDING)
    @PutMapping("/update/vehicle/{requestId}")
    public ResponseEntity<?> updateVehicleRequest(
            @PathVariable Integer requestId,
            @RequestBody VehicleRequestDTO vehicleRequest) {
        try {
            ApprovalRequest updatedRequest = approvalService.updateRequest(requestId, vehicleRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // UPDATE: Customer updates policy creation request (only if PENDING)
    @PutMapping("/update/policy/{requestId}")
    public ResponseEntity<?> updatePolicyRequest(
            @PathVariable Integer requestId,
            @RequestBody PolicyRequestDTO policyRequest) {
        try {
            ApprovalRequest updatedRequest = approvalService.updatePolicyRequest(requestId, policyRequest);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // DELETE: Customer deletes request (only if PENDING)
    @DeleteMapping("/delete/{requestId}")
    public ResponseEntity<?> deleteRequest(@PathVariable Integer requestId) {
        try {
            approvalService.deleteRequest(requestId);
            return ResponseEntity.ok(new SuccessResponse("Request deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
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

    // Get request by ID
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestById(@PathVariable Integer requestId) {
        try {
            ApprovalRequestDTO request = approvalService.getRequestById(requestId);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
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

    static class SuccessResponse {
        public String message;
        public SuccessResponse(String message) {
            this.message = message;
        }
    }

    static class CountResponse {
        public Long count;
        public CountResponse(Long count) {
            this.count = count;
        }
    }
}