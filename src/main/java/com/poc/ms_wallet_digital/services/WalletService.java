package com.poc.ms_wallet_digital.services;

import com.poc.ms_wallet_digital.controllers.requests.TransactionRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.TransactionResponseDTO;
import com.poc.ms_wallet_digital.controllers.responses.WalletBalanceResponseDTO;
import com.poc.ms_wallet_digital.controllers.responses.WalletResponseDTO;
import com.poc.ms_wallet_digital.enums.MovementTypeEnum;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.repositories.MovementRepository;
import com.poc.ms_wallet_digital.repositories.WalletRepository;
import com.poc.ms_wallet_digital.repositories.models.Movement;
import com.poc.ms_wallet_digital.repositories.models.Wallet;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    private final MeterRegistry meterRegistry;
    private final WalletRepository walletRepository;
    private final MovementRepository movementRepository;

    public WalletService(WalletRepository walletRepository,
                         MovementRepository movementRepository,
                         MeterRegistry meterRegistry, WalletRepository walletRepository1,
                         MovementRepository movementRepository1) {
        this.meterRegistry = meterRegistry;
        this.walletRepository = walletRepository1;
        this.movementRepository = movementRepository1;
    }

    @Transactional
    public TransactionResponseDTO processTransaction(TransactionRequestDTO dto) {

        Optional<Movement> existingMovement = movementRepository.findByTransferId(dto.getTransferId());

        if (existingMovement.isPresent()) {
            Movement movement = existingMovement.get();
            log.warn("Transaction {} already processed. Current status: {}", dto.getTransferId(), movement.getStatus());

            return new TransactionResponseDTO(movement.getTransferId(), movement.getStatus());
        }

        UUID walletId = UUID.fromString(dto.getId().toString());
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found with id: " + walletId));

        MovementTypeEnum movementType;
        StatusEnum transactionStatus = StatusEnum.COMPLETED;
        String failureReason = null;

        if ("DEPOSIT".equalsIgnoreCase(dto.getType().toString())) {
            wallet.setBalance(wallet.getBalance().add(dto.getAmount()));
            movementType = MovementTypeEnum.INCOMING;

        } else if ("WITHDRAW".equalsIgnoreCase(dto.getType().toString()) || "TRANSFER".equalsIgnoreCase(dto.getType().toString())) {
            movementType = MovementTypeEnum.OUTGOING;

            if (wallet.getBalance().compareTo(dto.getAmount()) < 0) {
                transactionStatus = StatusEnum.FAILED;
                failureReason = "Insufficient funds in wallet: " + walletId;
                log.error("Transaction {} failed: {}", dto.getTransferId(), failureReason);
            } else {
                wallet.setBalance(wallet.getBalance().subtract(dto.getAmount()));
            }

        } else {
            throw new IllegalArgumentException("Transaction type not supported: " + dto.getType());
        }

        if (transactionStatus == StatusEnum.COMPLETED) {
            walletRepository.save(wallet);
        }

        Movement movement = Movement.builder()
                .transferId(dto.getTransferId())
                .wallet(wallet)
                .type(movementType)
                .operation(dto.getType())
                .amount(dto.getAmount())
                .status(transactionStatus)
                .descriptionCounterpart(transactionStatus == StatusEnum.FAILED ? failureReason : "Movement Participant: " + dto.getCounterparty().id())
                .build();

        movementRepository.save(movement);

        String currentFlow = MDC.get("flow") != null ? MDC.get("flow") : "unknown_flow";
        String metricStatus = (transactionStatus == StatusEnum.COMPLETED) ? "SUCCESS" : "FAILED";

        Counter.builder("wallet.transactions.completed")
                .description("Total of financial transactions processed by the ledger engine")
                .tag("flow", currentFlow)
                .tag("status", metricStatus)
                .tag("type", dto.getType().toString())
                .register(meterRegistry)
                .increment();

        return new TransactionResponseDTO(movement.getTransferId(), movement.getStatus());
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDTO> findWallets(UUID clientId, UUID accountId) {
        List<Wallet> wallets = walletRepository.findByClientIdAndAccountId(clientId, accountId);

        return wallets.stream()
                .map(wallet -> new WalletResponseDTO(
                        wallet.getId(),
                        wallet.getName(),
                        wallet.getBalance(),
                        wallet.getClientId(),
                        wallet.getAccountId()
                ))
                .toList();
    }

    @Transactional
    public WalletResponseDTO initializeOrGetMainWallet(UUID clientId, UUID accountId) {
        List<Wallet> existingWallets = walletRepository.findByClientIdAndAccountId(clientId, accountId);

        if (!existingWallets.isEmpty()) {
            return mapToDTO(existingWallets.getFirst());
        }

        Wallet mainWallet = Wallet.builder()
                .name("Main Wallet")
                .balance(BigDecimal.ZERO)
                .clientId(clientId)
                .accountId(accountId)
                .build();

        return mapToDTO(walletRepository.save(mainWallet));
    }

    private WalletResponseDTO mapToDTO(Wallet wallet) {
        return new WalletResponseDTO(
                wallet.getId(),
                wallet.getName(),
                wallet.getBalance(),
                wallet.getClientId(),
                wallet.getAccountId()
        );
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponseDTO getBalanceAtDate(UUID walletId, OffsetDateTime atDate) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found with id: " + walletId));

        BigDecimal calculatedBalance = wallet.getBalance();
        OffsetDateTime targetDate = (atDate != null) ? atDate : OffsetDateTime.now();

        if (atDate != null && atDate.isBefore(OffsetDateTime.now())) {
            List<Movement> historicalMovements = movementRepository.findByWalletIdAndStatusAndDateTimeMovementAfter(walletId, StatusEnum.COMPLETED, targetDate);

            for (Movement movement : historicalMovements) {
                if (MovementTypeEnum.INCOMING == movement.getType()) {
                    calculatedBalance = calculatedBalance.subtract(movement.getAmount());
                } else if (MovementTypeEnum.OUTGOING == movement.getType()) {
                    calculatedBalance = calculatedBalance.add(movement.getAmount());
                }
            }
        }

        return new WalletBalanceResponseDTO(walletId, calculatedBalance, targetDate);
    }
}