package com.tugmamat.repo;

import com.tugmamat.model.MaterialRequest;
import com.tugmamat.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    List<MaterialRequest> findByRequesterOrderByCreatedAtDesc(User requester);
    long countByFulfilledFalse();
}
