package com.moneyPay.wallet.service;

import com.moneyPay.common.exception.MoneyPayException;
import com.moneyPay.wallet.domain.Wallet;
import com.moneyPay.wallet.domain.WalletTransaction;
import com.moneyPay.wallet.domain.WalletTransaction.TransactionType;
import com.moneyPay.wallet.repository.WalletRepository;
import com.moneyPay.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Wallet balance management — thread-safe using DB-level pessimistic locking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository            walletRepository;
    private final WalletTransactionRepository transactionRepository;

    /**
     * Credits the wallet for the given user.
     * Creates the wallet automatically if it doesn't yet exist (first-time user).
     */
    @Transactional
    public void credit(UUID userId, String currency, BigDecimal amount, String referenceId, String description) {
        if (transactionRepository.existsByReferenceId(referenceId + ":CREDIT")) {
            log.warn("Duplicate credit attempt ignored referenceId={}", referenceId);
            return;
        }

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, currency)
                .orElseGet(() -> createWallet(userId, currency));

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        saveTransaction(wallet, TransactionType.CREDIT, amount, referenceId + ":CREDIT",
                description, wallet.getBalance());
        log.info("Wallet credited userId={} amount={} {} newBalance={}",
                userId, amount, currency, wallet.getBalance());
    }

    /**
     * Debits the wallet. Throws {@code INSUFFICIENT_FUNDS} if balance would go negative.
     */
    @Transactional
    public void debit(UUID userId, String currency, BigDecimal amount, String referenceId, String description) {
        if (transactionRepository.existsByReferenceId(referenceId + ":DEBIT")) {
            log.warn("Duplicate debit attempt ignored referenceId={}", referenceId);
            return;
        }

        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, currency)
                .orElseThrow(() -> new MoneyPayException(
                        "Wallet not found", HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new MoneyPayException("Insufficient funds", HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet = walletRepository.save(wallet);

        saveTransaction(wallet, TransactionType.DEBIT, amount, referenceId + ":DEBIT",
                description, wallet.getBalance());
        log.info("Wallet debited userId={} amount={} {} newBalance={}",
                userId, amount, currency, wallet.getBalance());
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new MoneyPayException(
                        "Wallet not found", HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND"));
    }

    @Transactional(readOnly = true)
    public Page<WalletTransaction> getTransactions(UUID userId, Pageable pageable) {
        Wallet wallet = getWallet(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Wallet createWallet(UUID userId, String currency) {
        log.info("Auto-creating wallet for userId={} currency={}", userId, currency);
        return walletRepository.save(Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build());
    }

    private void saveTransaction(Wallet wallet, TransactionType type, BigDecimal amount,
                                 String referenceId, String description, BigDecimal balanceAfter) {
        transactionRepository.save(WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(type)
                .amount(amount)
                .referenceId(referenceId)
                .description(description)
                .balanceAfter(balanceAfter)
                .build());
    }
}
