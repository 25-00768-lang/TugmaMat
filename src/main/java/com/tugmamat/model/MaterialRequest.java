package com.tugmamat.model;

import com.tugmamat.model.Enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** The material a requester is looking for, for their project. */
@Entity
public class MaterialRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Enumerated(EnumType.STRING)
    public Category category;
    public Double lengthCm;
    public Double widthCm;
    public int quantity;
    @Enumerated(EnumType.STRING)
    public Condition desiredCondition;
    public String location;
    public boolean fulfilled;
    @ManyToOne
    public User requester;
    public LocalDateTime createdAt = LocalDateTime.now();

    public MaterialRequest() {}

    public String getSize() {
        if (lengthCm == null && widthCm == null) return "Any size";
        return Material.fmt(lengthCm) + " × " + Material.fmt(widthCm) + " cm";
    }
}
