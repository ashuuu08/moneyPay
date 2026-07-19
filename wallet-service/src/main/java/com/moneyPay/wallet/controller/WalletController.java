package com.moneyPay.wallet.controller;

import com.moneyPay.common.dto.ApiResponse;
import com.moneyPay.wallet.domain.Wallet;
import com.moneyPay.wallet.domain.WalletTransaction;
import com.moneyPay.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for wallet balance and transaction history queries.
 */
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet balance and transaction history APIs")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Get wallet balance for a user")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Wallet>> getWallet(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(walletService.getWallet(userId)));
    }

    @Operation(summary = "Get paginated transaction history for a user's wallet")
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<ApiResponse<Page<WalletTransaction>>> getTransactions(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(walletService.getTransactions(userId, pageable)));
    }
}
