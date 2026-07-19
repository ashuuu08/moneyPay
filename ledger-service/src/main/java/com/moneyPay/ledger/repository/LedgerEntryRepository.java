package com.moneyPay.ledger.repository;

import com.moneyPay.ledger.domain.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByPaymentId(UUID paymentId);

    Page<LedgerEntry> findByAccountIdOrderByPostedAtDesc(UUID accountId, Pageable pageable);

    boolean existsByPaymentIdAndEntryType(UUID paymentId, LedgerEntry.EntryType entryType);
}
