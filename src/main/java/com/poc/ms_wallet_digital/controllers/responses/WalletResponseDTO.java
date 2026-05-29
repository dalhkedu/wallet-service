package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletResponseDTO(
        UUID id,
        String name,
        UUID accountId,
        UUID clientId
) {


}
