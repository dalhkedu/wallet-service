package com.poc.ms_wallet_digital.controllers.responses;

import java.util.UUID;

public record WalletBalanceResponse(
        String name,
        UUID walletId
) {
}
