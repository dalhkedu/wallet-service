package com.poc.ms_wallet_digital.controllers.requests;

import java.math.BigDecimal;
import java.util.UUID;

public class FromRequest {

    private UUID walletId;
    private BigDecimal balance;

    public FromRequest(UUID walletId, BigDecimal balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
