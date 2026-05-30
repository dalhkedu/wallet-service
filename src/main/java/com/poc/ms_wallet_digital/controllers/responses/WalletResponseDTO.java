package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletResponseDTO(
        UUID id,
        String name,
        BigDecimal balance,
        UUID clientId,
        UUID accountId
) {


}
