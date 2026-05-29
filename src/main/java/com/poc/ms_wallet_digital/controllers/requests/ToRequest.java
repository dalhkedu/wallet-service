package com.poc.ms_wallet_digital.controllers.requests;

import java.util.UUID;

public class ToRequest {

    private UUID walletId;

    public ToRequest(UUID walletId) {
        this.walletId = walletId;
    }

    public UUID getWalletId() {
        return walletId;
    }
}
