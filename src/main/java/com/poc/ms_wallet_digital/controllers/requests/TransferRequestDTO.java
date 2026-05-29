package com.poc.ms_wallet_digital.controllers.requests;

public class TransferRequestDTO {

    private String transferId;
    private FromRequest from;
    private ToRequest to;

    public TransferRequestDTO(String transferId, FromRequest from, ToRequest to) {
        this.transferId = transferId;
        this.from = from;
        this.to = to;
    }

    public String getTransferId() {
        return transferId;
    }

    public FromRequest getFrom() {
        return from;
    }

    public ToRequest getTo() {
        return to;
    }
}
