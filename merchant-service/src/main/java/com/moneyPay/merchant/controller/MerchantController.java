package com.moneyPay.merchant.controller;

import com.moneyPay.common.dto.ApiResponse;
import com.moneyPay.merchant.domain.Merchant;
import com.moneyPay.merchant.domain.MerchantApiKey;
import com.moneyPay.merchant.dto.MerchantOnboardRequest;
import com.moneyPay.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for merchant onboarding, KYC, and API key management.
 */
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchants", description = "Merchant onboarding, KYC, and API key management")
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "Onboard a new merchant")
    @PostMapping
    public ResponseEntity<ApiResponse<Merchant>> onboard(
            @Valid @RequestBody MerchantOnboardRequest request) {
        Merchant merchant = merchantService.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Merchant onboarded", merchant));
    }

    @Operation(summary = "Get merchant profile by ID")
    @GetMapping("/{merchantId}")
    public ResponseEntity<ApiResponse<Merchant>> getMerchant(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(ApiResponse.ok(merchantService.getMerchant(merchantId)));
    }

    @Operation(summary = "Update KYC status (admin only)")
    @PatchMapping("/{merchantId}/kyc")
    public ResponseEntity<ApiResponse<Merchant>> updateKyc(
            @PathVariable UUID merchantId,
            @RequestParam Merchant.KycStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(merchantService.updateKycStatus(merchantId, status)));
    }

    @Operation(summary = "Generate a new API key for the merchant")
    @PostMapping("/{merchantId}/api-keys")
    public ResponseEntity<ApiResponse<String>> generateApiKey(
            @PathVariable UUID merchantId,
            @RequestParam(required = false) String label) {
        String plainKey = merchantService.generateApiKey(merchantId, label);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("API key generated — save it now, it will not be shown again", plainKey));
    }

    @Operation(summary = "List active API keys for the merchant")
    @GetMapping("/{merchantId}/api-keys")
    public ResponseEntity<ApiResponse<List<MerchantApiKey>>> listApiKeys(
            @PathVariable UUID merchantId) {
        return ResponseEntity.ok(ApiResponse.ok(merchantService.listApiKeys(merchantId)));
    }

    @Operation(summary = "Revoke an API key")
    @DeleteMapping("/api-keys/{keyId}")
    public ResponseEntity<ApiResponse<Void>> revokeApiKey(@PathVariable UUID keyId) {
        merchantService.revokeApiKey(keyId);
        return ResponseEntity.ok(ApiResponse.ok("API key revoked", null));
    }
}
