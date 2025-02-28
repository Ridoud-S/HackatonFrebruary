package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Solo necesita una declaración del método
    List<Transaction> findByStatus(TransactionStatus status);
}