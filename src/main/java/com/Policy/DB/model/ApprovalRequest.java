package com.Policy.DB.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ApprovalRequest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String requestData; // JSON string containing the request details

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminComments;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "processedAt")
    private LocalDateTime processedAt;

    @Column(name = "processedBy")
    private String processedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}