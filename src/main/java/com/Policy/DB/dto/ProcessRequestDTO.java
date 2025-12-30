package com.Policy.DB.dto;

import com.Policy.DB.model.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestDTO {
    private RequestStatus status; // APPROVED or REJECTED
    private String adminComments;
}