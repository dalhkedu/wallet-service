package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import com.poc.ms_wallet_digital.controllers.requests.FromRequest;

public class WithdrawRequestDTO {

    @NotBlank(message = "The 'transferId' field is required and cannot be blank.")
    private String transferId;

    @NotEmpty(message = "The 'from' field is required and cannot be empty.")
    private FromRequest from;

    public WithdrawRequestDTO(String transferId, FromRequest from) {
        this.transferId = transferId;
        this.from = from;
    }

    public String getTransferId() {
        return transferId;
    }

    public FromRequest getFrom() {
        return from;
    }
}
