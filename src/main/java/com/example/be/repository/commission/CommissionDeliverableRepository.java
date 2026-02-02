package com.example.be.repository.commission;

import com.example.be.entity.CommissionDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommissionDeliverableRepository extends JpaRepository<CommissionDeliverable, Long> {

    List<CommissionDeliverable> findByCommissionRequestIdOrderByCreatedAtDesc(Long commissionRequestId);
    Optional<CommissionDeliverable> findByIdAndCommissionRequestId(Long id, Long commissionRequestId);

}
