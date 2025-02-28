// src/main/java/com/hackathon/blockchain/model/WalletKey.java
package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class WalletKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String publicKey;

    @Lob
    private String privateKey;

    @OneToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
}