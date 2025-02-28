// src/main/java/com/hackathon/blockchain/model/SmartContract.java
package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SmartContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String conditionExpression;  // Ej: "amount > 1000"
    private String action;               // Ej: "CANCEL_TRANSACTION"
    private String actionValue;          // Ej: "10" (para fees)
    private String issuerWalletId;       // Wallet que emite el contrato
    private String digitalSignature;     // Firma RSA del contrato
    private String status = "ACTIVE";    // ACTIVE/INACTIVE
}