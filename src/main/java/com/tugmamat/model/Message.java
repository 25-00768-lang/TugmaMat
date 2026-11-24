package com.tugmamat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @ManyToOne
    public Material listing;
    @ManyToOne
    public User sender;
    @ManyToOne
    public User recipient;
    @Column(length = 1000)
    public String content;
    public LocalDateTime createdAt = LocalDateTime.now();

    public Message() {}
}
