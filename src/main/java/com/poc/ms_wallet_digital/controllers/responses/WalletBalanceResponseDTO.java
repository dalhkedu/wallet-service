package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletBalanceResponseDTO {

    private String name;
    private BigDecimal amount;

    public WalletBalanceResponseDTO(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;    }
}
