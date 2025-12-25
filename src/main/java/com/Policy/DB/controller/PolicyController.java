package com.Policy.DB.controller;
import com.Policy.DB.model.Policy;
import com.Policy.DB.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/policies")
@CrossOrigin(origins = "*")
public class PolicyController {
    @Autowired
    private PolicyService policyService;

    // Create new policy
    @PostMapping("/create/{vehicleId}")
    public ResponseEntity<Policy> createPolicy(
            @RequestBody Policy policy,
            @PathVariable Integer vehicleId) {
        try {
            Policy savedPolicy = policyService.createPolicy(policy, vehicleId);
            return new ResponseEntity<>(savedPolicy, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error"+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // Get policy details
    @GetMapping("/{policyId}")
    public ResponseEntity<Policy> getPolicyDetails(@PathVariable Integer policyId) {
        try {
            Policy policy = policyService.getPolicyDetails(policyId);
            return new ResponseEntity<>(policy, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // Renew policy
    @PutMapping("/renew/{policyId}")
    public ResponseEntity<Policy> renewPolicy(
            @PathVariable Integer policyId,
            @RequestParam String endDate) {
        try {
            LocalDate newEndDate = LocalDate.parse(endDate);
            Policy renewedPolicy = policyService.renewPolicy(policyId, newEndDate);
            return new ResponseEntity<>(renewedPolicy, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // Get all policies
    @GetMapping("/all")
    public ResponseEntity<List<Policy>> getAllPolicies() {
        List<Policy> policies = policyService.getAllPolicies();
        return new ResponseEntity<>(policies, HttpStatus.OK);
    }
    // Get policies by customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Policy>> getPoliciesByCustomerId(@PathVariable Integer customerId) {
        List<Policy> policies = policyService.getPoliciesByCustomerId(customerId);
        return new ResponseEntity<>(policies, HttpStatus.OK);
    }
    // Get active policies
    @GetMapping("/active")
    public ResponseEntity<List<Policy>> getActivePolicies() {
        List<Policy> policies = policyService.getActivePolicies();
        return new ResponseEntity<>(policies, HttpStatus.OK);
    }

    // Get expired policies
    @GetMapping("/expired")
    public ResponseEntity<List<Policy>> getExpiredPolicies() {
        List<Policy> policies = policyService.getExpiredPolicies();
        return new ResponseEntity<>(policies, HttpStatus.OK);
    }
}
