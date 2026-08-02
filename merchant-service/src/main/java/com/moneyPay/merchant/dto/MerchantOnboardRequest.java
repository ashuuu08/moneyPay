package com.moneyPay.merchant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MerchantOnboardRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "businessName is required")
    @Size(max = 200)
    private String businessName;

    @NotBlank(message = "businessEmail is required")
    @Email
    private String businessEmail;

    @Size(max = 20)
    private String businessPhone;

    @Size(max = 20)
    private String gstin;

    @Size(max = 10)
    private String pan;

    @Size(max = 500)
    private String webhookUrl;
}
