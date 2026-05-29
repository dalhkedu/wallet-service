package com.poc.ms_wallet_digital.controllers;

import com.poc.ms_wallet_digital.controllers.requests.DepositRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.TransferRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.WalletCreateRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.WithdrawRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.*;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/wallets") // 1. Correção da rota base da API
public class WalletsController {

    // UC01: Criar Carteira
    @PostMapping
    public ResponseEntity<DataResponse<WalletResponseDTO>> createWallet(@RequestBody WalletCreateRequestDTO request) {
        // Regra de negócio aqui...
        WalletResponseDTO response = new WalletResponseDTO(UUID.randomUUID(), "Wallet", UUID.randomUUID(), UUID.randomUUID()); // Simulação do retorno
        return ResponseEntity.status(HttpStatus.CREATED).body(DataResponse.of(response)); // Retorna 201 Created
    }

    // UC02: Consultar Carteiras por Cliente
    @GetMapping
    public ResponseEntity<DataResponse<WalletListResponseDTO>> getWalletsByClient(
            @RequestParam UUID clientId,
            @RequestParam UUID accountId) {

        WalletListResponseDTO list = new WalletListResponseDTO(/* lista de carteiras */);

        // Se não encontrar, service lança exceção capturada por um @ControllerAdvice que retorna 404

        // Retorna {"data": { "wallets": [...] }}
        return ResponseEntity.ok(DataResponse.of(list)); // Retorna 200 OK
    }

    // UC03: Consultar Saldo (Atual ou Histórico)
    @GetMapping("/{walletId}")
    public ResponseEntity<DataResponse<WalletBalanceResponseDTO>> getWalletBalance(
            @PathVariable UUID walletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime atDate) {

        WalletBalanceResponseDTO balance = new WalletBalanceResponseDTO("Main Wallet", BigDecimal.valueOf(1500.50));

        return ResponseEntity.ok(DataResponse.of(balance));
    }

    // UC08: Transferência entre Carteiras (Force/Síncrono pelo Orquestrador)
    @PostMapping("/{walletId}/transfer") // Alinhado com o endpoint do contrato "/balance"
    public ResponseEntity<DataResponse<TransactionResponseDTO>> transferBalance(
            @PathVariable UUID walletId,
            @RequestBody TransferRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(
                        new TransactionResponseDTO(
                                "35cbb982-9947-451c-a651-db79595c6c53_timestamp",
                                StatusEnum.COMPLETED)));
    }

    // UC06: Depositar Fundos (Conta -> Carteira)
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> depositFunds(
            @PathVariable UUID walletId,
            @RequestBody DepositRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(
                        new TransactionResponseDTO(
                                "35cbb982-9947-451c-a651-db79595c6c53_timestamp",
                                StatusEnum.COMPLETED)));
    }

    // UC07: Sacar Fundos (Carteira -> Conta)
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<DataResponse<TransactionResponseDTO>> withdrawFunds(
            @PathVariable UUID walletId,
            @RequestBody WithdrawRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataResponse.of(
                        new TransactionResponseDTO(
                                "35cbb982-9947-451c-a651-db79595c6c53_timestamp",
                                StatusEnum.COMPLETED)));
    }
}
