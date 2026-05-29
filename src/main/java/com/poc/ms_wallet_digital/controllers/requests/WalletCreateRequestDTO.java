package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class WalletCreateRequestDTO {

    @NotBlank(message = "Client ID is required")
    private UUID clientId;

    @NotBlank(message = "Account ID is required")
    private UUID accountId;

    public WalletCreateRequestDTO(UUID clientId, UUID accountId) {
        this.clientId = clientId;
        this.accountId = accountId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }
}
