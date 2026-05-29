package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record WalletCreateRequestDTO(

        @NotBlank(message = "Client ID is required")
        UUID clientId,
        @NotBlank(message = "Account ID is required")
        UUID accountId
) {
}
