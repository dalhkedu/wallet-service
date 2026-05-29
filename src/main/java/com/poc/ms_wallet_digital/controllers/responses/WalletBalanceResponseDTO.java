package com.poc.ms_wallet_digital.controllers.responses;

import java.math.BigDecimal;

public class WalletBalanceResponseDTO {

    private String name;
    private BigDecimal balance;

    public WalletBalanceResponseDTO(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
