package com.Policy.DB.repository;

import com.Policy.DB.model.ApprovalRequest;
import com.Policy.DB.model.RequestStatus;
import com.Policy.DB.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Integer> {
    List<ApprovalRequest> findByStatus(RequestStatus status);
    List<ApprovalRequest> findByCustomer_CustomerId(Integer customerId);
    List<ApprovalRequest> findByCustomer_CustomerIdAndStatus(Integer customerId, RequestStatus status);
    List<ApprovalRequest> findByRequestTypeAndStatus(RequestType requestType, RequestStatus status);
    Long countByStatus(RequestStatus status);
}