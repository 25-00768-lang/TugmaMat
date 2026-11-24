package com.tugmamat.repo;

import com.tugmamat.model.Claim;
import com.tugmamat.model.Material;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByMaterialOrderByCreatedAtDesc(Material material);
    List<Claim> findAllByOrderByCreatedAtDesc();
}
