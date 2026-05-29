package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class ToRequest {

    @NotBlank(message = "The 'id account or wallet' field is required and cannot be blank.")
    private UUID id;

    public ToRequest(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
