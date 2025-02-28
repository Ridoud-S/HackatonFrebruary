// src/main/java/com/hackathon/blockchain/model/User.java
package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
}