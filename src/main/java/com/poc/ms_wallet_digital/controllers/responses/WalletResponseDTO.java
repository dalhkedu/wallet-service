package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletResponseDTO {
    private UUID id;
    private String name;
    private UUID accountId;
    private UUID clientId;

    public WalletResponseDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public WalletResponseDTO(UUID id, String name, UUID accountId, UUID clientId) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
        this.clientId = clientId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }
}
