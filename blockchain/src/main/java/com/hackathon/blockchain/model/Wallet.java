// src/main/java/com/hackathon/blockchain/model/Wallet.java
package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private Double balance;
    private String accountStatus;
    private Double netWorth;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asset> assets = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public List<Asset> getAssets()
    {
        return assets;
    }
}