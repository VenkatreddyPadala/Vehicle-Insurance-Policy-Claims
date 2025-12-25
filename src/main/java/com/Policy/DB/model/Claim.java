package com.Policy.DB.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Claim")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer claimId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policyId", nullable = false)
    private Policy policy;

    @Column(precision = 10, scale = 2)
    private BigDecimal claimAmount;

    @Column(columnDefinition = "TEXT")
    private String claimReason;

    private LocalDate claimDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('SUBMITTED','APPROVED','REJECTED')")
    private ClaimStatus claimStatus;
}