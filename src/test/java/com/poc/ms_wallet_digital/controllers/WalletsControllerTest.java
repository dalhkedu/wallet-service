package com.poc.ms_wallet_digital.controllers;

import com.poc.ms_wallet_digital.advices.GlobalExceptionHandler;
import com.poc.ms_wallet_digital.controllers.requests.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({WalletsController.class, GlobalExceptionHandler.class})
@DisplayName("Testes do WalletsController")
class WalletsControllerTest {

    private static final String BASE_URL = "/wallets";
    private static final UUID TEST_WALLET_ID = UUID.randomUUID();
    private static final UUID TEST_CLIENT_ID = UUID.randomUUID();
    private static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== UC01: Criar Carteira ====================
    @Test
    @DisplayName("Deve criar uma carteira com sucesso retornando 201 CREATED")
    void testCreateWalletSuccess() throws Exception {
        WalletCreateRequestDTO request = new WalletCreateRequestDTO();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao criar carteira sem body")
    void testCreateWalletMissingBody() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== UC02: Consultar Carteiras por Cliente ====================
    @Test
    @DisplayName("Deve retornar lista de carteiras com sucesso retornando 200 OK")
    void testGetWalletsByClientSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("clientId", TEST_CLIENT_ID.toString())
                        .param("accountId", TEST_ACCOUNT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando falta o parâmetro clientId")
    void testGetWalletsWithoutClientId() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("accountId", TEST_ACCOUNT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando falta o parâmetro accountId")
    void testGetWalletsWithoutAccountId() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("clientId", TEST_CLIENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 com IDs inválidos (UUID inválido)")
    void testGetWalletsWithInvalidUUIDs() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("clientId", "invalid-uuid")
                        .param("accountId", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== UC03: Consultar Saldo ====================
    @Test
    @DisplayName("Deve retornar saldo atual da carteira retornando 200 OK")
    void testGetWalletBalanceSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + TEST_WALLET_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name", is("Main Wallet")))
                .andExpect(jsonPath("$.data.balance", is(1500.50)));
    }

    @Test
    @DisplayName("Deve retornar saldo em data específica quando parâmetro atDate é fornecido")
    void testGetWalletBalanceWithAtDateParameter() throws Exception {
        String atDate = "2024-01-15T10:30:00-03:00";

        mockMvc.perform(get(BASE_URL + "/" + TEST_WALLET_ID)
                        .param("atDate", atDate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 com walletId inválido (UUID inválido)")
    void testGetWalletBalanceWithInvalidUUID() throws Exception {
        mockMvc.perform(get(BASE_URL + "/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve depositar fundos com sucesso retornando 201 CREATED")
    void testDepositFundsSuccess() throws Exception {
        DepositRequestDTO request = new DepositRequestDTO();

        mockMvc.perform(post(BASE_URL + "/" + TEST_WALLET_ID + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao depositar sem body")
    void testDepositFundsMissingBody() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + TEST_WALLET_ID + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao depositar com walletId inválido")
    void testDepositFundsWithInvalidWalletId() throws Exception {
        DepositRequestDTO request = new DepositRequestDTO();

        mockMvc.perform(post(BASE_URL + "/invalid-uuid/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve sacar fundos com sucesso retornando 201 CREATED")
    void testWithdrawFundsSuccess() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO();

        mockMvc.perform(post(BASE_URL + "/" + TEST_WALLET_ID + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao sacar sem body")
    void testWithdrawFundsMissingBody() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + TEST_WALLET_ID + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao sacar com walletId inválido")
    void testWithdrawFundsWithInvalidWalletId() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO();

        mockMvc.perform(post(BASE_URL + "/invalid-uuid/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== UC08: Transferência entre Carteiras ====================
    @Test
    @DisplayName("Deve transferir saldo entre carteiras com sucesso retornando 201 CREATED")
    void testTransferBalanceSuccess() throws Exception {
        UUID toWalletId = UUID.randomUUID();
        FromRequest fromRequest = new FromRequest(TEST_WALLET_ID, new BigDecimal("100.00"));
        ToRequest toRequest = new ToRequest(toWalletId);
        TransferRequestDTO request = new TransferRequestDTO("TRANSFER-001", fromRequest, toRequest);

        mockMvc.perform(post(BASE_URL + "/" + TEST_WALLET_ID + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao transferir sem body")
    void testTransferBalanceMissingBody() throws Exception {
        mockMvc.perform(post(BASE_URL + "/" + TEST_WALLET_ID + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao transferir com walletId inválido")
    void testTransferBalanceWithInvalidWalletId() throws Exception {
        UUID toWalletId = UUID.randomUUID();
        FromRequest fromRequest = new FromRequest(TEST_WALLET_ID, new BigDecimal("100.00"));
        ToRequest toRequest = new ToRequest(toWalletId);
        TransferRequestDTO request = new TransferRequestDTO("TRANSFER-001", fromRequest, toRequest);

        mockMvc.perform(post(BASE_URL + "/invalid-uuid/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Testes de URLMapping ====================
    @Test
    @DisplayName("Deve ter a rota base correta /wallets")
    void testBaseURLMapping() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("clientId", TEST_CLIENT_ID.toString())
                        .param("accountId", TEST_ACCOUNT_ID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 404 para rota inexistente")
    void testNotFoundForInvalidRoute() throws Exception {
        mockMvc.perform(get( "/invalid-route"))
                .andExpect(status().isNotFound());
    }

}

