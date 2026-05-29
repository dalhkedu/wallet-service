package com.poc.ms_wallet_digital.controllers.responses;

import java.util.UUID;

public class WalletBalanceResponse {
    private String name;
    private UUID walletId;

    public WalletBalanceResponse(UUID walletId, String name) {
    }

    public String getName() {
        return name;
    }

    public UUID getWalletId() {
        return walletId;
    }
}
