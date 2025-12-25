package com.Policy.DB.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer policyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicleId", nullable = false)
    private Vehicle vehicle;

    @Column(length = 20)
    private String policyNumber;

    @Column(precision = 10, scale = 2)
    private BigDecimal coverageAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal premiumAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ACTIVE','EXPIRED')")
    private PolicyStatus policyStatus;

    @JsonIgnore
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims;
}