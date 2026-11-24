package com.tugmamat.model;

import com.tugmamat.model.Enums.ClaimStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A requester taking some pieces from a listing. Pieces are deducted at once and handed over at pickup. */
@Entity
public class Claim {
    public static final int PICKUP_DAYS = 3;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne
    public Material material;
    @ManyToOne
    public User requester;
    public int quantity;
    @Enumerated(EnumType.STRING)
    public ClaimStatus status = ClaimStatus.PENDING;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime closedAt;

    public Claim() {}

    /** Older rows created before pickup tracking have no status: treat them as picked up. */
    public ClaimStatus getState() { return status == null ? ClaimStatus.PICKED_UP : status; }
    public boolean isPending() { return getState() == ClaimStatus.PENDING; }
    public LocalDateTime getExpiresAt() { return createdAt.plusDays(PICKUP_DAYS); }
}
