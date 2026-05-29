package com.poc.ms_wallet_digital.controllers.responses;

import java.util.List;

public class WalletListResponseDTO {

    private List<WalletResponseDTO> listWallet;

    public WalletListResponseDTO(List<WalletResponseDTO> listWallet) {
        this.listWallet = listWallet;
    }

    public List<WalletResponseDTO> getListWallet() {
        return listWallet;
    }
}
