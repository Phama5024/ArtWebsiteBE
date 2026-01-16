package com.example.be.repository.commission;

import com.example.be.entity.CommissionRequest;
import com.example.be.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionRequestRepository
        extends JpaRepository<CommissionRequest, Long> {

    List<CommissionRequest> findByUserId(Long userId);

    List<CommissionRequest> findBySellerId(Long sellerId);

    List<CommissionRequest> findByStatus(CommissionStatus status);

    List<CommissionRequest> findBySellerIdAndStatus(
            Long sellerId, CommissionStatus status
    );
}
