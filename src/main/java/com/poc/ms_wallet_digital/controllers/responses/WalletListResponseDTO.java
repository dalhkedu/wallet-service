package com.poc.ms_wallet_digital.controllers.responses;

import java.util.List;

public record WalletListResponseDTO(

        List<WalletResponseDTO> listWallet
) {
}

