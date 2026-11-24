package com.tugmamat.repo;

import com.tugmamat.model.Enums.Status;
import com.tugmamat.model.Material;
import com.tugmamat.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByStatusOrderByCreatedAtDesc(Status status);
    List<Material> findByOwnerOrderByCreatedAtDesc(User owner);
    List<Material> findAllByOrderByReportedDescCreatedAtDesc();
    long countByStatus(Status status);
}
