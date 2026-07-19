package com.moneyPay.ledger.service;

import com.moneyPay.ledger.domain.LedgerEntry;
import com.moneyPay.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Double-entry bookkeeping service.
 *
 * <p>For every successful payment, posts exactly two ledger entries atomically:
 * <ol>
 *   <li>DEBIT  on the buyer's account</li>
 *   <li>CREDIT on the merchant's account</li>
 * </ol>
 * The operation is idempotent — duplicate payment IDs are silently ignored.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    /**
     * Posts the double-entry pair for a successful payment.
     * Idempotent: if entries already exist for this paymentId they are not duplicated.
     */
    @Transactional
    public void postDoubleEntry(UUID paymentId,
                                UUID userId,
                                UUID merchantId,
                                BigDecimal amount,
                                String currency,
                                String orderId) {

        // Idempotency guard
        if (ledgerEntryRepository.existsByPaymentIdAndEntryType(paymentId, LedgerEntry.EntryType.DEBIT)) {
            log.warn("Ledger entries already exist for paymentId={} — skipping duplicate", paymentId);
            return;
        }

        LedgerEntry debit = LedgerEntry.builder()
                .paymentId(paymentId)
                .accountId(userId)
                .entryType(LedgerEntry.EntryType.DEBIT)
                .amount(amount)
                .currency(currency)
                .description("Payment sent — orderId=" + orderId)
                .build();

        LedgerEntry credit = LedgerEntry.builder()
                .paymentId(paymentId)
                .accountId(merchantId)
                .entryType(LedgerEntry.EntryType.CREDIT)
                .amount(amount)
                .currency(currency)
                .description("Payment received — orderId=" + orderId)
                .build();

        ledgerEntryRepository.saveAll(List.of(debit, credit));
        log.info("Double-entry posted paymentId={} amount={} {} debit={} credit={}",
                paymentId, amount, currency, userId, merchantId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntriesByPaymentId(UUID paymentId) {
        return ledgerEntryRepository.findByPaymentId(paymentId);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntry> getEntriesByAccountId(UUID accountId, Pageable pageable) {
        return ledgerEntryRepository.findByAccountIdOrderByPostedAtDesc(accountId, pageable);
    }
}
