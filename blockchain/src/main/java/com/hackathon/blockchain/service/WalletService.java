package com.hackathon.blockchain.service;

import com.hackathon.blockchain.model.*;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataService marketDataService;
    private final BlockchainService blockchainService;

    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletByAddress(String address) {
        return walletRepository.findByAddress(address);
    }

    @Transactional
    public void initializeLiquidityPools(Map<String, Double> initialAssets) {
        initialAssets.forEach((symbol, quantity) -> {
            String liquidityAddress = "LP-" + symbol;
            walletRepository.findByAddress(liquidityAddress).ifPresentOrElse(
                    existing -> log.info("Liquidity pool exists for {}", symbol),
                    () -> createLiquidityPool(symbol, quantity)
            );
        });
    }

    private void createLiquidityPool(String symbol, double quantity) {
        Wallet liquidityWallet = new Wallet();
        liquidityWallet.setAddress("LP-" + symbol);
        liquidityWallet.setBalance(0.0);
        liquidityWallet.setAccountStatus("ACTIVE");

        Asset asset = new Asset(symbol, quantity, liquidityWallet);
        liquidityWallet.getAssets().add(asset);
        walletRepository.save(liquidityWallet);
    }

    @Transactional
    public String buyAsset(Long userId, String symbol, double quantity) {
        return executeAssetTransaction(userId, symbol, quantity, TransactionType.BUY);
    }

    @Transactional
    public String sellAsset(Long userId, String symbol, double quantity) {
        return executeAssetTransaction(userId, symbol, quantity, TransactionType.SELL);
    }

    private String executeAssetTransaction(Long userId, String symbol, double quantity, TransactionType type) {
        try {
            WalletOperation operation = validateTransaction(userId, symbol, quantity, type);
            double price = marketDataService.fetchLivePriceForAsset(symbol);

            processTransaction(operation, price, quantity, type);
            recordTransaction(operation, symbol, quantity, price, type);

            return "✅ " + type + " completed successfully!";
        } catch (WalletException e) {
            log.error("Transaction failed: {}", e.getMessage());
            return e.getMessage();
        }
    }

    private WalletOperation validateTransaction(Long userId, String symbol, double quantity, TransactionType type)
            throws WalletException {

        Wallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        Wallet liquidityWallet = walletRepository.findByAddress("LP-" + symbol)
                .orElseThrow(() -> new WalletException("Liquidity pool not found"));

        if(type == TransactionType.BUY && !symbol.equals("USDT")) {
            validateUSDTBalance(userWallet, symbol, quantity);
        }

        return new WalletOperation(userWallet, liquidityWallet, type, symbol);
    }

    private void validateUSDTBalance(Wallet wallet, String symbol, double quantity) throws WalletException {
        double requiredUSDT = calculateRequiredUSDT(symbol, quantity);
        Optional<Asset> usdtAsset = findAsset(wallet, "USDT");

        if(usdtAsset.isEmpty() || usdtAsset.get().getQuantity() < requiredUSDT) {
            throw new WalletException("Insufficient USDT balance");
        }
    }

    private double calculateRequiredUSDT(String symbol, double quantity) {
        return quantity * marketDataService.fetchLivePriceForAsset(symbol);
    }

    private void processTransaction(WalletOperation operation, double price, double quantity, TransactionType type) {
        switch(type) {
            case BUY -> processBuyOperation(operation, price, quantity);
            case SELL -> processSellOperation(operation, price, quantity);
            default -> log.warn("Unsupported transaction type: {}", type);
        }
    }

    private void processBuyOperation(WalletOperation operation, double price, double quantity) {
        Wallet userWallet = operation.userWallet();
        Wallet liquidityWallet = operation.liquidityWallet();

        updateAsset(userWallet, "USDT", -quantity * price);
        updateAsset(liquidityWallet, "USDT", quantity * price);
        updateAsset(userWallet, operation.symbol(), quantity);
        updateAsset(liquidityWallet, operation.symbol(), -quantity);

        walletRepository.saveAll(List.of(userWallet, liquidityWallet));
    }

    private void processSellOperation(WalletOperation operation, double price, double quantity) {
        Wallet userWallet = operation.userWallet();
        Wallet liquidityWallet = operation.liquidityWallet();

        updateAsset(userWallet, operation.symbol(), -quantity);
        updateAsset(liquidityWallet, operation.symbol(), quantity);
        updateAsset(userWallet, "USDT", quantity * price);
        updateAsset(liquidityWallet, "USDT", -quantity * price);

        walletRepository.saveAll(List.of(userWallet, liquidityWallet));
    }

    private void updateAsset(Wallet wallet, String symbol, double amount) {
        Optional<Asset> assetOpt = findAsset(wallet, symbol);

        if(assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            asset.addQuantity(amount);
            if(asset.getQuantity() <= 0) {
                wallet.getAssets().remove(asset);
            }
        } else if(amount > 0) {
            wallet.getAssets().add(new Asset(symbol, amount, wallet));
        }
    }

    private Optional<Asset> findAsset(Wallet wallet, String symbol) {
        return wallet.getAssets().stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(symbol))
                .findFirst();
    }

    private void recordTransaction(WalletOperation operation, String symbol,
                                   double quantity, double price, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setSenderWallet(type == TransactionType.BUY ? operation.liquidityWallet() : operation.userWallet());
        transaction.setReceiverWallet(type == TransactionType.BUY ? operation.userWallet() : operation.liquidityWallet());
        transaction.setAssetSymbol(symbol);
        transaction.setAmount(quantity);
        transaction.setPricePerUnit(price);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.save(transaction);
    }

    @Transactional
    public void transferFee(Transaction tx, double feeValue) {
        Wallet sender = tx.getSenderWallet();
        Optional<Wallet> feeWalletOpt = walletRepository.findByAddress("FEES-USDT");

        feeWalletOpt.ifPresent(feeWallet -> {
            updateAsset(sender, "USDT", -feeValue);
            updateAsset(feeWallet, "USDT", feeValue);
            walletRepository.saveAll(List.of(sender, feeWallet));

            Transaction feeTransaction = new Transaction();
            feeTransaction.setSenderWallet(sender);
            feeTransaction.setReceiverWallet(feeWallet);
            feeTransaction.setAssetSymbol("USDT");
            feeTransaction.setAmount(feeValue);
            feeTransaction.setType(TransactionType.FEE);
            feeTransaction.setStatus(TransactionStatus.COMPLETED);
            feeTransaction.setTimestamp(LocalDateTime.now());
            transactionRepository.save(feeTransaction);
        });
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateWalletBalances() {
        walletRepository.findAll().forEach(wallet -> {
            double total = wallet.getBalance() + wallet.getAssets().stream()
                    .mapToDouble(a -> a.getQuantity() * marketDataService.fetchLivePriceForAsset(a.getSymbol()))
                    .sum();
            wallet.setNetWorth(total);
            walletRepository.save(wallet);
        });
    }

    private record WalletOperation(
            Wallet userWallet,
            Wallet liquidityWallet,
            TransactionType type,
            String symbol
    ) {}

    private static class WalletException extends Exception {
        public WalletException(String message) {
            super(message);
        }
    }
}