package com.moneyPay.merchant.service;

import com.moneyPay.common.exception.MoneyPayException;
import com.moneyPay.merchant.domain.Merchant;
import com.moneyPay.merchant.domain.MerchantApiKey;
import com.moneyPay.merchant.dto.MerchantOnboardRequest;
import com.moneyPay.merchant.repository.MerchantApiKeyRepository;
import com.moneyPay.merchant.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Merchant onboarding and API key management service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository       merchantRepository;
    private final MerchantApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder    passwordEncoder;

    @Transactional
    public Merchant onboard(MerchantOnboardRequest req) {
        UUID userId = UUID.fromString(req.getUserId());
        if (merchantRepository.existsByUserId(userId)) {
            throw new MoneyPayException("Merchant already registered for this user",
                    HttpStatus.CONFLICT, "MERCHANT_ALREADY_EXISTS");
        }

        Merchant merchant = Merchant.builder()
                .userId(userId)
                .businessName(req.getBusinessName())
                .businessEmail(req.getBusinessEmail())
                .businessPhone(req.getBusinessPhone())
                .gstin(req.getGstin())
                .pan(req.getPan())
                .webhookUrl(req.getWebhookUrl())
                .build();

        merchant = merchantRepository.save(merchant);
        log.info("Merchant onboarded merchantId={} business={}", merchant.getId(), merchant.getBusinessName());
        return merchant;
    }

    @Transactional(readOnly = true)
    public Merchant getMerchant(UUID merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MoneyPayException(
                        "Merchant not found", HttpStatus.NOT_FOUND, "MERCHANT_NOT_FOUND"));
    }

    @Transactional
    public Merchant updateKycStatus(UUID merchantId, Merchant.KycStatus newStatus) {
        Merchant merchant = getMerchant(merchantId);
        merchant.setKycStatus(newStatus);
        log.info("KYC status updated merchantId={} status={}", merchantId, newStatus);
        return merchantRepository.save(merchant);
    }

    // ── API Key Management ──────────────────────────────────────────────────

    /**
     * Generates a new API key for the merchant.
     * The plain-text key is returned ONCE — never stored in plain text.
     *
     * @return plain-text API key (show to merchant once, then discard)
     */
    @Transactional
    public String generateApiKey(UUID merchantId, String label) {
        getMerchant(merchantId); // validate existence

        // Generate: "mpk_live_" + 32 random bytes base64url-encoded
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        String plainKey = "mpk_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String prefix   = plainKey.substring(0, 17); // "mpk_live_XXXXXXXX"

        MerchantApiKey apiKey = MerchantApiKey.builder()
                .merchantId(merchantId)
                .keyPrefix(prefix)
                .keyHash(passwordEncoder.encode(plainKey))
                .label(label != null ? label : "Default")
                .build();

        apiKeyRepository.save(apiKey);
        log.info("API key generated merchantId={} prefix={}", merchantId, prefix);
        return plainKey; // Return once — cannot be recovered
    }

    @Transactional(readOnly = true)
    public List<MerchantApiKey> listApiKeys(UUID merchantId) {
        return apiKeyRepository.findByMerchantIdAndActiveTrue(merchantId);
    }

    @Transactional
    public void revokeApiKey(UUID keyId) {
        apiKeyRepository.findById(keyId).ifPresent(key -> {
            key.setActive(false);
            apiKeyRepository.save(key);
            log.info("API key revoked keyId={}", keyId);
        });
    }
}
