package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.poc.ms_wallet_digital.enums.StatusEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponseDTO(

        String transferId,
        StatusEnum status) {
}
