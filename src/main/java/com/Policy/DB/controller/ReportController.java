package com.Policy.DB.controller;
import com.Policy.DB.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping("/claims/download")
    public ResponseEntity<byte[]> downloadClaimReport() {
        byte[] pdf = reportService.generateClaimReportPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "claim_report.pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
    @GetMapping("/policies/download")
    public ResponseEntity<byte[]> downloadPolicyReport() {
        byte[] pdf = reportService.generatePolicyReportPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "policy_report.pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
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
    @GetMapping("/customer/{customerId}/download")
    public ResponseEntity<byte[]> downloadCustomerReport(@PathVariable Integer customerId) {
        byte[] pdf = reportService.generateCustomerReportPdf(customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "customer_" + customerId + "_report.pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
    @GetMapping("/claims/download/csv")
    public ResponseEntity<byte[]> downloadClaimReportCsv() {
        byte[] csv = reportService.generateClaimReportCsv();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "claim_report.csv");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    // Download policy report as CSV
    @GetMapping("/policies/download/csv")
    public ResponseEntity<byte[]> downloadPolicyReportCsv() {
        byte[] csv = reportService.generatePolicyReportCsv();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "policy_report.csv");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    // Download customer report as CSV
    @GetMapping("/customer/{customerId}/download/csv")
    public ResponseEntity<byte[]> downloadCustomerReportCsv(@PathVariable Integer customerId) {
        byte[] csv = reportService.generateCustomerReportCsv(customerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "customer_" + customerId + "_report.csv");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}
