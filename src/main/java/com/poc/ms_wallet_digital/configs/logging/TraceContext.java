package com.poc.ms_wallet_digital.configs.logging;

import org.slf4j.MDC;
import java.util.UUID;

public final class TraceContext {

    public static void enrich(UUID clientId, UUID accountId, UUID walletId) {
        if (clientId != null) MDC.put("client_id", clientId.toString());
        if (accountId != null) MDC.put("account_id", accountId.toString());
        if (walletId != null) MDC.put("wallet_id", walletId.toString());
    }

    public static void enrichWallet(UUID walletId) {
        if (walletId != null) MDC.put("wallet_id", walletId.toString());
    }
}
