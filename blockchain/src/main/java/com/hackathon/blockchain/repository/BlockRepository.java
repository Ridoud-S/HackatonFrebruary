// En: src/main/java/com/hackathon/blockchain/repository/BlockRepository.java
package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findAll(Sort sort);
}