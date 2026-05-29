package com.poc.ms_wallet_digital.controllers.requests;


import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionRequestDTO {
    private String transferId;
    private UUID id;
    private TransactionTypeEnum type;
    private BigDecimal amount;
    private CounterpartyRequestDTO counterparty;

    public TransactionRequestDTO() {
    }

    public TransactionRequestDTO(String transferId, UUID id, TransactionTypeEnum type, BigDecimal amount, CounterpartyRequestDTO counterparty) {
        this.transferId = transferId;
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.counterparty = counterparty;
    }

    public TransactionRequestDTO(String transferId, BigDecimal amount, CounterpartyRequestDTO counterparty) {
        this.transferId = transferId;
        this.amount = amount;
        this.counterparty = counterparty;
    }

    public UUID getId() {
        return id;
    }

    public String getTransferId() {
        return transferId;
    }

    public TransactionTypeEnum getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CounterpartyRequestDTO getCounterparty() {
        return counterparty;
    }

    public void setType(TransactionTypeEnum type) {
        this.type = type;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

