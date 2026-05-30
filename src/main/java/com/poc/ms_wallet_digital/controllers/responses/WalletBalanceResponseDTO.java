package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletBalanceResponseDTO(

        UUID walletId,
        BigDecimal balance,
        OffsetDateTime asOfDate

) {
}
