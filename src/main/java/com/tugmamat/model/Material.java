package com.tugmamat.model;

import com.tugmamat.model.Enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A leftover material posted by a provider. */
@Entity
public class Material {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Enumerated(EnumType.STRING)
    public Category category;
    @Column(length = 1000)
    public String description;
    public Double lengthCm;
    public Double widthCm;
    public int quantity;
    public String unit;
    @Enumerated(EnumType.STRING)
    public Condition matCondition;
    public String location;
    public String imageUrl;
    @Enumerated(EnumType.STRING)
    public Status status = Status.AVAILABLE;
    public boolean reported;
    @ManyToOne
    public User owner;
    public LocalDateTime createdAt = LocalDateTime.now();

    public Material() {}

    public static String fmt(Double d) {
        if (d == null) return "?";
        return d == Math.floor(d) ? String.valueOf(d.longValue()) : String.valueOf(d);
    }

    public String getSize() {
        if (lengthCm == null && widthCm == null) return "Not specified";
        return fmt(lengthCm) + " × " + fmt(widthCm) + " cm";
    }
}
