package com.poc.ms_wallet_digital.controllers.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.poc.ms_wallet_digital.enums.StatusEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseDTO {

    private String transferId;
    private StatusEnum status;

    public TransactionResponseDTO(String transferId, StatusEnum status) {
        this.transferId = transferId;
        this.status = status;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }
}
