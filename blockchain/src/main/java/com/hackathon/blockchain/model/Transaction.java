package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_wallet_id")
    private Wallet senderWallet;

    @ManyToOne
    @JoinColumn(name = "receiver_wallet_id")
    private Wallet receiverWallet;

    private String assetSymbol;
    private double amount;
    private double pricePerUnit;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime timestamp = LocalDateTime.now();



}