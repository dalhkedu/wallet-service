package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.UUID;

public class FromRequest {

    @NotBlank(message = "The 'id account or wallets' field is required and cannot be blank.")
    private UUID id;

    @NotEmpty(message = "The 'amount' field is required and cannot be blank.")
    @Min(value = 0, message = "The 'amount' field must be a positive value.")
    private BigDecimal amount;

    public FromRequest(UUID id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
