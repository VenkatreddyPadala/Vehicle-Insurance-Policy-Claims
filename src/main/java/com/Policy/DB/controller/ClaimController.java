package com.Policy.DB.controller;
import com.Policy.DB.model.Claim;
import com.Policy.DB.model.ClaimStatus;
import com.Policy.DB.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/claims")
@CrossOrigin(origins = "*")
public class ClaimController {
    @Autowired
    private ClaimService claimService;
    // File a new claim
    @PostMapping("/file/{policyId}")
    public ResponseEntity<Claim> fileClaim(
            @RequestBody Claim claim,
            @PathVariable Integer policyId) {
        try {
            Claim savedClaim = claimService.fileClaim(claim, policyId);
            return new ResponseEntity<>(savedClaim, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error"+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get claim status
    @GetMapping("/{claimId}")
    public ResponseEntity<Claim> getClaimStatus(@PathVariable Integer claimId) {
        try {
            Claim claim = claimService.getClaimStatus(claimId);
            return new ResponseEntity<>(claim, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error"+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Process claim (approve/reject)
    @PutMapping("/process/{claimId}")
    public ResponseEntity<Claim> processClaim(
            @PathVariable Integer claimId,
            @RequestParam String status) {
        try {
            ClaimStatus claimStatus = ClaimStatus.valueOf(status.toUpperCase());
            Claim processedClaim = claimService.processClaim(claimId, claimStatus);
            return new ResponseEntity<>(processedClaim, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error"+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get all claims
    @GetMapping("/all")
    public ResponseEntity<List<Claim>> getAllClaims() {
        List<Claim> claims = claimService.getAllClaims();
        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    // Get claims by policy
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<Claim>> getClaimsByPolicyId(@PathVariable Integer policyId) {
        List<Claim> claims = claimService.getClaimsByPolicyId(policyId);
        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    // Get claims by customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Claim>> getClaimsByCustomerId(@PathVariable Integer customerId) {
        List<Claim> claims = claimService.getClaimsByCustomerId(customerId);
        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    // Get pending claims
    @GetMapping("/pending")
    public ResponseEntity<List<Claim>> getPendingClaims() {
        List<Claim> claims = claimService.getPendingClaims();
        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    // Get claims by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Claim>> getClaimsByStatus(@PathVariable String status) {
        try {
            ClaimStatus claimStatus = ClaimStatus.valueOf(status.toUpperCase());
            List<Claim> claims = claimService.getClaimsByStatus(claimStatus);
            return new ResponseEntity<>(claims, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error"+e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
