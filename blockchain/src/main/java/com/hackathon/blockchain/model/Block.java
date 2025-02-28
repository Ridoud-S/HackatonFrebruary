// En: src/main/java/com/hackathon/blockchain/model/Block.java
package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int blockIndex;
    private String previousHash;
    private long timestamp;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    private int nonce;
    private String hash;

    // Método para calcular el hash
    public String calculateHash() {
        String dataToHash = blockIndex + previousHash + timestamp + transactions.hashCode() + nonce;
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(dataToHash);
    }
}