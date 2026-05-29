package com.poc.ms_wallet_digital.controllers.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import com.poc.ms_wallet_digital.controllers.requests.ToRequest;

import java.math.BigDecimal;

public class DepositRequestDTO {

    @NotBlank(message = "The 'transferId' field is required and cannot be blank.")
    private String transferId;

    @NotEmpty(message = "The 'amount' field is required and cannot be blank.")
    @Min(value = 0, message = "The 'amount' field must be a positive value.")
    private BigDecimal amount;

    @NotEmpty(message = "The 'to' field is required and cannot be empty.")
    private ToRequest to;

    public DepositRequestDTO(String transferId, BigDecimal amount, ToRequest to) {
        this.transferId = transferId;
        this.amount = amount;
        this.to = to;
    }

    public String getTransferId() {
        return transferId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ToRequest getTo() {
        return to;
    }

}
