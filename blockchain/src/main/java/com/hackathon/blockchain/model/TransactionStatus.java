package com.hackathon.blockchain.model;

public enum TransactionStatus {
    PENDING,
    CANCELED,
    PROCESSED_CONTRACT, // Nuevo estado
    COMPLETED,
    PROCESSED, FAILED
}