package com.Policy.DB.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PolicyNumberGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1000);

    /**
     * Generates a unique policy number
     * Format: POL-YYYYMMDD-XXXX
     * Example: POL-20241225-1001
     */
    public String generatePolicyNumber() {
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = counter.incrementAndGet();

        return String.format("POL-%s-%04d", datePart, sequence);
    }

    /**
     * Generates policy number with vehicle type prefix
     * Format: [TYPE]-YYYYMMDD-XXXX
     * Example: CAR-20241225-1001, BIKE-20241225-1002
     */
    public String generatePolicyNumberWithType(String vehicleType) {
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = counter.incrementAndGet();

        return String.format("%s-%s-%04d", vehicleType, datePart, sequence);
    }
}