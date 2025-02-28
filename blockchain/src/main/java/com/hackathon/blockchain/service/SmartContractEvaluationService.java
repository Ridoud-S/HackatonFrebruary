// SmartContractEvaluationService.java
package com.hackathon.blockchain.service;

import com.hackathon.blockchain.model.*;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.utils.SignatureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.PublicKey;
import java.util.List;

@Slf4j
@Service
public class SmartContractEvaluationService {

    private final SmartContractRepository smartContractRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final WalletKeyService walletKeyService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public SmartContractEvaluationService(SmartContractRepository smartContractRepository,
                                          TransactionRepository transactionRepository,
                                          WalletService walletService,
                                          WalletKeyService walletKeyService) {
        this.smartContractRepository = smartContractRepository;
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.walletKeyService = walletKeyService;
    }

    public boolean verifyContractSignature(SmartContract contract) {
        try
        {
            // Convertir issuerWalletId a Long si es necesario
            Long walletId = Long.parseLong(contract.getIssuerWalletId());
            PublicKey issuerPublicKey = walletKeyService.getPublicKeyForWallet(walletId);

            if (issuerPublicKey == null) {
                log.warn("Public key not found for wallet: {}", contract.getIssuerWalletId());
                return false;
            }

            String dataToSign = contract.getName()
                    + contract.getConditionExpression()
                    + contract.getAction()
                    + contract.getActionValue()
                    + contract.getIssuerWalletId();

            return SignatureUtil.verifySignature(
                    dataToSign,
                    contract.getDigitalSignature(),
                    issuerPublicKey
            );
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void evaluateSmartContracts() {
        log.info("Evaluating smart contracts...");

        List<SmartContract> contracts = smartContractRepository.findAll();
        List<Transaction> pendingTxs = transactionRepository.findByStatus(TransactionStatus.PENDING);

        for (Transaction tx : pendingTxs) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("amount", tx.getAmount());
            context.setVariable("txType", tx.getType().name());

            for (SmartContract contract : contracts) {
                if (!verifyContractSignature(contract)) continue;

                try {
                    Expression exp = parser.parseExpression(contract.getConditionExpression());
                    Boolean conditionMet = exp.getValue(context, Boolean.class);

                    if (Boolean.TRUE.equals(conditionMet)) {
                        executeContractAction(contract, tx);
                    }
                } catch (Exception e) {
                    log.error("Error evaluating contract {}: {}", contract.getName(), e.getMessage());
                }
            }
        }
    }

    private void executeContractAction(SmartContract contract, Transaction tx) {
        switch (contract.getAction().toUpperCase()) {
            case "CANCEL_TRANSACTION" -> {
                tx.setStatus(TransactionStatus.CANCELED);
                transactionRepository.save(tx);
                if (log.isInfoEnabled()) {
                    log.info("Transaction {} canceled", tx.getId());
                }
            }
            case "TRANSFER_FEE" ->
            {
                double feeValue = Double.parseDouble(contract.getActionValue());
                walletService.transferFee(tx, feeValue);
                tx.setStatus(TransactionStatus.PROCESSED);
                transactionRepository.save(tx);
                log.info("Fee transferred for transaction {}", tx.getId());
            }
            default -> log.warn("Unsupported contract action: {}", contract.getAction());
        }
    }
}