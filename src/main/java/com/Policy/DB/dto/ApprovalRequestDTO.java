package com.Policy.DB.dto;

import com.Policy.DB.model.RequestStatus;
import com.Policy.DB.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {
    private Integer requestId;
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private RequestType requestType;
    private String requestData;
    private RequestStatus status;
    private String adminComments;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String processedBy;
}