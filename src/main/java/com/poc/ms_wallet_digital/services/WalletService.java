package com.poc.ms_wallet_digital.services;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final MeterRegistry meterRegistry;

    public WalletService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void executeDeposit(UUID walletId, BigDecimal amount) {


        Counter.builder("wallet.transactions.completed")
                .description("Total de transações financeiras liquidadas com sucesso")
                .tag("flow", MDC.get("flow"))
                .tag("status", "SUCCESS")
                .register(meterRegistry)
                .increment();
    }
}