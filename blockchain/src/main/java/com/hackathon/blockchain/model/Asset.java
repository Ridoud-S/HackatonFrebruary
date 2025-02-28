package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String symbol; // Ej: BTC, ETH, USDT

    @Column(nullable = false)
    private Double quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // Constructor para fácil creación de instancias
    public Asset(String symbol, Double quantity, Wallet wallet) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.wallet = wallet;
    }

    // Método helper para actualizar cantidades
    public void addQuantity(Double amount) {
        this.quantity += amount;
        if(this.quantity < 0) {
            throw new IllegalArgumentException("Cantidad no puede ser negativa");
        }
    }
}