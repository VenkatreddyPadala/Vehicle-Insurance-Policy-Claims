package com.Policy.DB.service;

import com.Policy.DB.model.Claim;
import com.Policy.DB.model.Policy;
import com.Policy.DB.model.ClaimStatus;
import com.Policy.DB.model.Customer;
import com.Policy.DB.repository.ClaimRepository;
import com.Policy.DB.repository.PolicyRepository;
import com.Policy.DB.repository.CustomerRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportService {
    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Generate claim report (JSON)
    public Map<String, Object> generateClaimReport() {
        Map<String, Object> report = new HashMap<>();

        List<Claim> allClaims = claimRepository.findAll();
        report.put("totalClaims", allClaims.size());

        BigDecimal approvedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.APPROVED);
        BigDecimal rejectedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.REJECTED);
        BigDecimal submittedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.SUBMITTED);

        report.put("approvedClaimAmount", approvedAmount != null ? approvedAmount : BigDecimal.ZERO);
        report.put("rejectedClaimAmount", rejectedAmount != null ? rejectedAmount : BigDecimal.ZERO);
        report.put("submittedClaimAmount", submittedAmount != null ? submittedAmount : BigDecimal.ZERO);

        List<Claim> pendingClaims = claimRepository.findPendingClaims();
        report.put("pendingClaims", pendingClaims.size());

        return report;
    }

    // Generate claim report (PDF)
    public byte[] generateClaimReportPdf() {
        try {
            List<Claim> allClaims = claimRepository.findAll();

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("CLAIM REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Generated date
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Summary statistics
            BigDecimal approvedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.APPROVED);
            BigDecimal rejectedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.REJECTED);
            BigDecimal submittedAmount = claimRepository.getTotalClaimAmountByStatus(ClaimStatus.SUBMITTED);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addSummaryRow(summaryTable, "Total Claims:", String.valueOf(allClaims.size()));
            addSummaryRow(summaryTable, "Approved Amount:", "₹" + (approvedAmount != null ? approvedAmount : BigDecimal.ZERO));
            addSummaryRow(summaryTable, "Rejected Amount:", "₹" + (rejectedAmount != null ? rejectedAmount : BigDecimal.ZERO));
            addSummaryRow(summaryTable, "Pending Amount:", "₹" + (submittedAmount != null ? submittedAmount : BigDecimal.ZERO));

            document.add(summaryTable);

            // Claims table
            PdfPTable claimsTable = new PdfPTable(7);
            claimsTable.setWidthPercentage(100);
            claimsTable.setWidths(new float[]{1, 2, 2, 2, 2, 1.5f, 3});

            // Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            String[] headers = {"ID", "Policy", "Customer", "Amount", "Date", "Status", "Reason"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.BLACK);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                claimsTable.addCell(cell);
            }

            // Data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 8);
            for (Claim claim : allClaims) {
                claimsTable.addCell(new Phrase(String.valueOf(claim.getClaimId()), cellFont));
                claimsTable.addCell(new Phrase(claim.getPolicy() != null ? claim.getPolicy().getPolicyNumber() : "N/A", cellFont));
                claimsTable.addCell(new Phrase(
                        claim.getPolicy() != null && claim.getPolicy().getVehicle() != null &&
                                claim.getPolicy().getVehicle().getCustomer() != null ?
                                claim.getPolicy().getVehicle().getCustomer().getName() : "N/A", cellFont));
                claimsTable.addCell(new Phrase("₹" + claim.getClaimAmount(), cellFont));
                claimsTable.addCell(new Phrase(claim.getClaimDate().toString(), cellFont));
                claimsTable.addCell(new Phrase(claim.getClaimStatus().toString(), cellFont));
                claimsTable.addCell(new Phrase(claim.getClaimReason(), cellFont));
            }

            document.add(claimsTable);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating claim report PDF", e);
        }
    }

    // Generate policy report (JSON)
    public Map<String, Object> generatePolicyReport() {
        Map<String, Object> report = new HashMap<>();

        List<Policy> allPolicies = policyRepository.findAll();
        report.put("totalPolicies", allPolicies.size());

        List<Policy> activePolicies = policyRepository.findByPolicyStatus(com.Policy.DB.model.PolicyStatus.ACTIVE);
        report.put("activePolicies", activePolicies.size());

        List<Policy> expiredPolicies = policyRepository.findByPolicyStatus(com.Policy.DB.model.PolicyStatus.EXPIRED);
        report.put("expiredPolicies", expiredPolicies.size());

        BigDecimal totalPremium = allPolicies.stream()
                .map(Policy::getPremiumAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalPremiumCollected", totalPremium);

        return report;
    }

    // Generate policy report (PDF)
    public byte[] generatePolicyReportPdf() {
        try {
            List<Policy> allPolicies = policyRepository.findAll();

            Document document = new Document(PageSize.A4.rotate()); // Landscape for more columns
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("POLICY REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Generated date
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Summary statistics
            List<Policy> activePolicies = policyRepository.findByPolicyStatus(com.Policy.DB.model.PolicyStatus.ACTIVE);
            List<Policy> expiredPolicies = policyRepository.findByPolicyStatus(com.Policy.DB.model.PolicyStatus.EXPIRED);
            BigDecimal totalPremium = allPolicies.stream()
                    .map(Policy::getPremiumAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setSpacingAfter(20);

            addSummaryRow(summaryTable, "Total Policies:", String.valueOf(allPolicies.size()));
            addSummaryRow(summaryTable, "Active Policies:", String.valueOf(activePolicies.size()));
            addSummaryRow(summaryTable, "Expired Policies:", String.valueOf(expiredPolicies.size()));
            addSummaryRow(summaryTable, "Total Premium Collected:", "₹" + totalPremium);

            document.add(summaryTable);

            // Policies table
            PdfPTable policiesTable = new PdfPTable(9);
            policiesTable.setWidthPercentage(100);
            policiesTable.setWidths(new float[]{1, 2, 2, 2.5f, 2, 2, 2, 2, 1.5f});

            // Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            String[] headers = {"ID", "Policy #", "Customer", "Vehicle", "Coverage", "Premium", "Start Date", "End Date", "Status"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.BLACK);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                policiesTable.addCell(cell);
            }

            // Data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 8);
            for (Policy policy : allPolicies) {
                policiesTable.addCell(new Phrase(String.valueOf(policy.getPolicyId()), cellFont));
                policiesTable.addCell(new Phrase(policy.getPolicyNumber(), cellFont));
                policiesTable.addCell(new Phrase(
                        policy.getVehicle() != null && policy.getVehicle().getCustomer() != null ?
                                policy.getVehicle().getCustomer().getName() : "N/A", cellFont));
                policiesTable.addCell(new Phrase(
                        policy.getVehicle() != null ?
                                policy.getVehicle().getMake() + " " + policy.getVehicle().getModel() : "N/A", cellFont));
                policiesTable.addCell(new Phrase("₹" + policy.getCoverageAmount(), cellFont));
                policiesTable.addCell(new Phrase("₹" + policy.getPremiumAmount(), cellFont));
                policiesTable.addCell(new Phrase(policy.getStartDate().toString(), cellFont));
                policiesTable.addCell(new Phrase(policy.getEndDate().toString(), cellFont));
                policiesTable.addCell(new Phrase(policy.getPolicyStatus().toString(), cellFont));
            }

            document.add(policiesTable);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating policy report PDF", e);
        }
    }

    // Generate customer report (JSON)
    public Map<String, Object> generateCustomerReport(Integer customerId) {
        Map<String, Object> report = new HashMap<>();

        List<Policy> customerPolicies = policyRepository.findByCustomerId(customerId);
        report.put("totalPolicies", customerPolicies.size());

        List<Claim> customerClaims = claimRepository.findByCustomerId(customerId);
        report.put("totalClaims", customerClaims.size());

        BigDecimal totalClaimAmount = customerClaims.stream()
                .map(Claim::getClaimAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalClaimAmount", totalClaimAmount);

        return report;
    }

    // Generate customer report (PDF)
    public byte[] generateCustomerReportPdf(Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            List<Policy> customerPolicies = policyRepository.findByCustomerId(customerId);
            List<Claim> customerClaims = claimRepository.findByCustomerId(customerId);

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("CUSTOMER REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Generated date
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Customer Information
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph customerSection = new Paragraph("Customer Information", sectionFont);
            customerSection.setSpacingAfter(10);
            document.add(customerSection);

            PdfPTable customerTable = new PdfPTable(2);
            customerTable.setWidthPercentage(100);
            customerTable.setSpacingAfter(20);

            addSummaryRow(customerTable, "Customer ID:", String.valueOf(customer.getCustomerId()));
            addSummaryRow(customerTable, "Name:", customer.getName());
            addSummaryRow(customerTable, "Email:", customer.getEmail());
            addSummaryRow(customerTable, "Phone:", customer.getPhone());

            document.add(customerTable);

            // Policies Section
            Paragraph policySection = new Paragraph("Policies (" + customerPolicies.size() + ")", sectionFont);
            policySection.setSpacingAfter(10);
            document.add(policySection);

            if (!customerPolicies.isEmpty()) {
                PdfPTable policiesTable = new PdfPTable(7);
                policiesTable.setWidthPercentage(100);
                policiesTable.setWidths(new float[]{2, 2.5f, 2, 2, 2, 2, 1.5f});
                policiesTable.setSpacingAfter(20);

                Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
                String[] policyHeaders = {"Policy #", "Vehicle", "Coverage", "Premium", "Start", "End", "Status"};
                for (String header : policyHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.BLACK);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    policiesTable.addCell(cell);
                }

                Font cellFont = new Font(Font.FontFamily.HELVETICA, 8);
                for (Policy policy : customerPolicies) {
                    policiesTable.addCell(new Phrase(policy.getPolicyNumber(), cellFont));
                    policiesTable.addCell(new Phrase(
                            policy.getVehicle() != null ?
                                    policy.getVehicle().getMake() + " " + policy.getVehicle().getModel() : "N/A", cellFont));
                    policiesTable.addCell(new Phrase("₹" + policy.getCoverageAmount(), cellFont));
                    policiesTable.addCell(new Phrase("₹" + policy.getPremiumAmount(), cellFont));
                    policiesTable.addCell(new Phrase(policy.getStartDate().toString(), cellFont));
                    policiesTable.addCell(new Phrase(policy.getEndDate().toString(), cellFont));
                    policiesTable.addCell(new Phrase(policy.getPolicyStatus().toString(), cellFont));
                }

                document.add(policiesTable);
            } else {
                Paragraph noPolicies = new Paragraph("No policies found.", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
                noPolicies.setSpacingAfter(20);
                document.add(noPolicies);
            }

            // Claims Section
            BigDecimal totalClaimAmount = customerClaims.stream()
                    .map(Claim::getClaimAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Paragraph claimSection = new Paragraph("Claims (" + customerClaims.size() + " - Total: ₹" + totalClaimAmount + ")", sectionFont);
            claimSection.setSpacingAfter(10);
            document.add(claimSection);

            if (!customerClaims.isEmpty()) {
                PdfPTable claimsTable = new PdfPTable(5);
                claimsTable.setWidthPercentage(100);
                claimsTable.setWidths(new float[]{2, 2, 2, 1.5f, 3});

                Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
                String[] claimHeaders = {"Policy #", "Amount", "Date", "Status", "Reason"};
                for (String header : claimHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.BLACK);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    claimsTable.addCell(cell);
                }

                Font cellFont = new Font(Font.FontFamily.HELVETICA, 8);
                for (Claim claim : customerClaims) {
                    claimsTable.addCell(new Phrase(
                            claim.getPolicy() != null ? claim.getPolicy().getPolicyNumber() : "N/A", cellFont));
                    claimsTable.addCell(new Phrase("₹" + claim.getClaimAmount(), cellFont));
                    claimsTable.addCell(new Phrase(claim.getClaimDate().toString(), cellFont));
                    claimsTable.addCell(new Phrase(claim.getClaimStatus().toString(), cellFont));
                    claimsTable.addCell(new Phrase(claim.getClaimReason(), cellFont));
                }

                document.add(claimsTable);
            } else {
                Paragraph noClaims = new Paragraph("No claims found.", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
                document.add(noClaims);
            }

            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating customer report PDF", e);
        }
    }

    // Helper method to add summary rows
    private void addSummaryRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
}