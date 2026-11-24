package com.tugmamat.model;

import com.tugmamat.model.Enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String fullName;
    @Column(unique = true)
    public String email;
    public String password;
    @Enumerated(EnumType.STRING)
    public Role role;
    public String location;

    public User() {}
}
