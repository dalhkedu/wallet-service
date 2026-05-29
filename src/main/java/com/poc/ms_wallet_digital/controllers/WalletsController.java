package com.poc.ms_wallet_digital.controllers;

import com.poc.ms_wallet_digital.configs.logging.TraceContext;
import com.poc.ms_wallet_digital.controllers.requests.TransactionRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.WalletCreateRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.*;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/wallets")
@Tag(name = "Digital Wallets Engine", description = "Endpoints for managing digital wallets, including balance inquiries, transfers, deposits, and withdrawals.")
public class WalletsController {

    private static final Logger log = LoggerFactory.getLogger(WalletsController.class);

    @Operation(
            summary = "Consulting Wallet Balance",
            description = "Searches for the current balance of a specific wallet. Optionally, " +
                    "you can specify a date to retrieve the balance at that point in time. If no date is provided, the current balance will be returned."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully."),
            @ApiResponse(responseCode = "400", description = "Contract failure. The sent ID is not a valid UUID."),
            @ApiResponse(responseCode = "404", description = "The specified wallet was not found in the database.")
    })

    @PostMapping
    public ResponseEntity<DataResponse<WalletResponseDTO>> createWallet(@RequestBody @NotNull WalletCreateRequestDTO request) {

        TraceContext.enrich(request.clientId(), request.accountId(), null);
        log.info("Creating wallet for clientId: {} and accountId: {}", request.clientId(), request.accountId());

        // Logica Verifica se existe na base sql a carteira para o cliente e conta, caso contrário, cria a carteira principal automaticamente

        WalletResponseDTO response = new WalletResponseDTO(
                UUID.randomUUID(), "Wallet", UUID.randomUUID(), UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.of(response));
    }


    @GetMapping
    public ResponseEntity<DataResponse<WalletListResponseDTO>> getWalletsByClient(
            @RequestParam UUID clientId,
            @RequestParam UUID accountId) {

        TraceContext.enrich(clientId, accountId, null);
        log.info("Consulting wallets for clientId: {} and accountId: {}", clientId, accountId);

        // Logica verifica se existe na base sql a carteira para o cliente e conta, caso nao existir,
        // retorna lista vazia com not found, caso contrário, retorna a lista de carteiras do cliente e conta

        WalletListResponseDTO list = new WalletListResponseDTO(
                List.of(new WalletResponseDTO(
                        UUID.randomUUID(), "Wallet", accountId, clientId))
        );

        return ResponseEntity.ok(DataResponse.of(list));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<DataResponse<WalletBalanceResponseDTO>> getWalletBalance(
            @PathVariable UUID walletId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.OffsetDateTime).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime atDate) {

        TraceContext.enrichWallet(walletId);
        log.info("Consulting wallet balance for walletId: {} and atDate: {}", walletId, atDate);

        // Logica verifica se existe na base sql a carteira para o walletId, caso nao existir, retorna not found,
        // caso contrário, retorna o saldo da carteira para a data especificada

        WalletBalanceResponseDTO balance = new WalletBalanceResponseDTO(
                "Main Wallet", BigDecimal.valueOf(1500.50));

        return ResponseEntity.ok(DataResponse.of(balance));
    }

    @PostMapping("/{walletId}/transfer")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> transferBalance(
            @PathVariable UUID walletId,
            @RequestBody @NotNull TransactionRequestDTO request) {

        request.setType(request.getType() == null ? TransactionTypeEnum.TRANSFER : request.getType());
        request.setId(walletId);

        TraceContext.enrichWallet(walletId);
        log.info("Starting transfer from walletId: {} to walletId: {} with amount: {}",
                walletId, request.getCounterparty().id(), request.getAmount());

        // Verifica se existe na base sql a carteira para o walletId, caso nao existir, retorna not found,
        // caso contrário, verifica se a carteira de destino existe, caso nao existir, retorna not found
        // caso contrário, verifica se o saldo é suficiente para a transferência, caso contrário, retorna bad request
        // caso contrário, realiza a transferência, atualiza os saldos das carteiras e retorna o status da transação

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(
                        new TransactionResponseDTO(
                                "35cbb982-9947-451c-a651-db79595c6c53_timestamp",
                                StatusEnum.COMPLETED)));
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> depositFunds(
            @PathVariable UUID walletId,
            @RequestBody @NotNull TransactionRequestDTO request) {

        request.setType(request.getType() == null ? TransactionTypeEnum.DEPOSIT : request.getType());
        request.setId(walletId);

        TraceContext.enrichWallet(walletId);
        log.info("Starting deposit to walletId: {} with amount: {}", walletId, request.getAmount());
        ;

        // Verifica se existe na base sql a carteira para o walletId, caso nao existir, retorna not found,
        // caso contrario, consulta em contas a conta de origem do depósito, caso nao existir, retorna not found
        // caso contrário, verifica se a conta de origem tem saldo suficiente para o depósito, caso contrário, retorna bad request
        // caso contrário, realiza o depósito, atualiza o saldo da conta de origem e da carteira de destino, e retorna o status da transação

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(
                        new TransactionResponseDTO(
                                "35cbb982-9947-451c-a651-db79595c6c53_timestamp",
                                StatusEnum.COMPLETED)));
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> withdrawFunds(
            @PathVariable UUID walletId,
            @RequestBody @NotNull TransactionRequestDTO request) {

        request.setType(request.getType() == null ? TransactionTypeEnum.WITHDRAW : request.getType());
        request.setId(walletId);

        TraceContext.enrichWallet(walletId);
        log.info("Starting withdrawal from walletId: {} with amount: {}", walletId, request.getAmount());

        // Verifica se existe na base sql a carteira para o walletId, caso nao existir, retorna not found,
        // caso contrário, consulta em contas a conta de destino do saque, caso nao existir, retorna not found
        // caso contrário, verifica se a carteira tem saldo suficiente para o saque, caso contrário, retorna bad request
        // caso contrário, realiza o saque, atualiza o saldo da carteira de origem e da conta de destino, e retorna o status da transação

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(
                        new TransactionResponseDTO(
                                "35cbb982-9947-451c-a651-db79595c6c53_timestamp",
                                StatusEnum.COMPLETED)));
    }
}
