package com.moneyPay.merchant.repository;

import com.moneyPay.merchant.domain.MerchantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, UUID> {
    List<MerchantApiKey> findByMerchantIdAndActiveTrue(UUID merchantId);
    Optional<MerchantApiKey> findByKeyPrefix(String keyPrefix);
}
