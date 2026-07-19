package com.moneyPay.ledger.controller;

import com.moneyPay.common.dto.ApiResponse;
import com.moneyPay.ledger.domain.LedgerEntry;
import com.moneyPay.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for ledger audit trail queries.
 */
@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "Double-entry bookkeeping audit trail APIs")
public class LedgerController {

    private final LedgerService ledgerService;

    @Operation(summary = "Get all ledger entries for a payment (audit trail)")
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<List<LedgerEntry>>> getByPayment(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(ledgerService.getEntriesByPaymentId(paymentId)));
    }

    @Operation(summary = "Get paginated ledger history for an account (user or merchant)")
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Page<LedgerEntry>>> getByAccount(
            @PathVariable UUID accountId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(ledgerService.getEntriesByAccountId(accountId, pageable)));
    }
}
