package com.poc.ms_wallet_digital.controllers;

import com.poc.ms_wallet_digital.configs.logging.TraceContext;
import com.poc.ms_wallet_digital.controllers.requests.TransactionRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.WalletCreateRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.*;
import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;
import com.poc.ms_wallet_digital.services.WalletService;
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

import java.time.OffsetDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/wallets")
@Tag(name = "Digital Wallets Engine", description = "Endpoints for managing digital wallets, including balance inquiries, transfers, deposits, and withdrawals.")
public class WalletsController {

    private static final Logger log = LoggerFactory.getLogger(WalletsController.class);
    private final WalletService walletService;

    public WalletsController(WalletService walletService) {
        this.walletService = walletService;
    }

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

        var wallet = walletService.initializeOrGetMainWallet(request.clientId(), request.accountId());

        return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.of(wallet));
    }


    @GetMapping
    public ResponseEntity<DataResponse<WalletListResponseDTO>> getWalletsByClient(
            @RequestParam UUID clientId,
            @RequestParam UUID accountId) {

        TraceContext.enrich(clientId, accountId, null);
        log.info("Consulting wallets for clientId: {} and accountId: {}", clientId, accountId);

        var wallets = walletService.findWallets(clientId, accountId);

        WalletListResponseDTO list = new WalletListResponseDTO(wallets);

        return ResponseEntity.ok(DataResponse.of(list));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<DataResponse<WalletBalanceResponseDTO>> getWalletBalance(
            @PathVariable UUID walletId,
            @RequestParam(required = false, defaultValue = "#{T(java.time.OffsetDateTime).now()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime atDate) {

        TraceContext.enrichWallet(walletId);
        log.info("Consulting wallet balance for walletId: {} and atDate: {}", walletId, atDate);

        var wallet = walletService.getBalanceAtDate(walletId, atDate);

        return ResponseEntity.ok(DataResponse.of(wallet));
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

        var movement = walletService.processTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(movement));
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> depositFunds(
            @PathVariable UUID walletId,
            @RequestBody @NotNull TransactionRequestDTO request) {

        request.setType(request.getType() == null ? TransactionTypeEnum.DEPOSIT : request.getType());
        request.setId(walletId);

        TraceContext.enrichWallet(walletId);
        log.info("Starting deposit to walletId: {} with amount: {}", walletId, request.getAmount());

        var movement = walletService.processTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(movement));
    }

    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> withdrawFunds(
            @PathVariable UUID walletId,
            @RequestBody @NotNull TransactionRequestDTO request) {

        request.setType(request.getType() == null ? TransactionTypeEnum.WITHDRAW : request.getType());
        request.setId(walletId);

        TraceContext.enrichWallet(walletId);
        log.info("Starting withdrawal from walletId: {} with amount: {}", walletId, request.getAmount());

        var movement = walletService.processTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(movement));
    }
}
