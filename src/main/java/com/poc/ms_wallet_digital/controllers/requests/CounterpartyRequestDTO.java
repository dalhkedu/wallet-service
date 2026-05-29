package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CounterpartyRequestDTO(
        @NotBlank(message = "The 'id account or wallets' field is required and cannot be blank.")
        UUID id
) {

}
