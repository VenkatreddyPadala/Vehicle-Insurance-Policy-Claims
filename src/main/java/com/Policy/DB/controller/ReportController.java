package com.Policy.DB.controller;
import com.Policy.DB.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    @Autowired
    private ReportService reportService;

    // Generate claim report
    @GetMapping("/claims")
    public ResponseEntity<Map<String, Object>> generateClaimReport() {
        Map<String, Object> report = reportService.generateClaimReport();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    // Generate policy report
    @GetMapping("/policies")
    public ResponseEntity<Map<String, Object>> generatePolicyReport() {
        Map<String, Object> report = reportService.generatePolicyReport();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    // Generate customer report
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> generateCustomerReport(@PathVariable Integer customerId) {
        Map<String, Object> report = reportService.generateCustomerReport(customerId);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
