package com.poc.ms_wallet_digital.services;

import com.poc.ms_wallet_digital.controllers.requests.CounterpartyRequestDTO;
import com.poc.ms_wallet_digital.controllers.requests.TransactionRequestDTO;
import com.poc.ms_wallet_digital.controllers.responses.TransactionResponseDTO;
import com.poc.ms_wallet_digital.controllers.responses.WalletBalanceResponseDTO;
import com.poc.ms_wallet_digital.controllers.responses.WalletResponseDTO;
import com.poc.ms_wallet_digital.enums.MovementTypeEnum;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;
import com.poc.ms_wallet_digital.repositories.MovementRepository;
import com.poc.ms_wallet_digital.repositories.WalletRepository;
import com.poc.ms_wallet_digital.repositories.models.Movement;
import com.poc.ms_wallet_digital.repositories.models.Wallet;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private MovementRepository movementRepository;

    private MeterRegistry meterRegistry;
    private WalletService walletService;

    private static final UUID TEST_WALLET_ID = UUID.randomUUID();
    private static final UUID TEST_CLIENT_ID = UUID.randomUUID();
    private static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();
    private static final String TEST_TRANSFER_ID = "tx-123456";
    private static final UUID TEST_COUNTERPARTY_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        walletService = new WalletService(walletRepository, movementRepository, meterRegistry, walletRepository, movementRepository);
    }

    @Test
    @DisplayName("Should return existing transaction status when transferId is already processed (Idempotency)")
    void shouldReturnExistingStatusOnIdempotentRequest() {

        var dto = new TransactionRequestDTO(
                TEST_TRANSFER_ID,
                TEST_WALLET_ID,
                TransactionTypeEnum.DEPOSIT,
                new BigDecimal("100.00"),
                new CounterpartyRequestDTO(TEST_COUNTERPARTY_ID));

        var mockMovement = Movement.builder().transferId(TEST_TRANSFER_ID).status(StatusEnum.COMPLETED).build();

        Mockito.when(movementRepository.findByTransferId(TEST_TRANSFER_ID)).thenReturn(Optional.of(mockMovement));

        TransactionResponseDTO response = walletService.processTransaction(dto);

        assertNotNull(response);
        assertEquals(TEST_TRANSFER_ID, response.transferId());
        assertEquals(StatusEnum.COMPLETED, response.status());
        Mockito.verifyNoInteractions(walletRepository);
    }

    @Test
    @DisplayName("Should process deposit successfully updating balance and creating an incoming movement")
    void shouldProcessDepositSuccessfully() {

        var dto = new TransactionRequestDTO(
                TEST_TRANSFER_ID,
                TEST_WALLET_ID,
                TransactionTypeEnum.DEPOSIT,
                new BigDecimal("150.00"),
                new CounterpartyRequestDTO(TEST_COUNTERPARTY_ID));

        var wallet = Wallet.builder().id(TEST_WALLET_ID).balance(new BigDecimal("100.00")).build();

        Mockito.when(movementRepository.findByTransferId(TEST_TRANSFER_ID)).thenReturn(Optional.empty());
        Mockito.when(walletRepository.findByIdForUpdate(TEST_WALLET_ID)).thenReturn(Optional.of(wallet));

        TransactionResponseDTO response = walletService.processTransaction(dto);

        assertNotNull(response);
        assertEquals(StatusEnum.COMPLETED, response.status());
        assertEquals(new BigDecimal("250.00"), wallet.getBalance());

        ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementRepository).save(movementCaptor.capture());
        assertEquals(MovementTypeEnum.INCOMING, movementCaptor.getValue().getType());
        assertEquals(StatusEnum.COMPLETED, movementCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should flag transaction as FAILED when processing a withdraw with insufficient funds")
    void shouldFailWithdrawWhenFundsAreInsufficient() {

        var dto = new TransactionRequestDTO(
                TEST_TRANSFER_ID,
                TEST_WALLET_ID,
                TransactionTypeEnum.DEPOSIT,
                new BigDecimal("100.00"),
                new CounterpartyRequestDTO(TEST_COUNTERPARTY_ID));

        var wallet = Wallet.builder().id(TEST_WALLET_ID).balance(new BigDecimal("50.00")).build();

        Mockito.when(movementRepository.findByTransferId(TEST_TRANSFER_ID)).thenReturn(Optional.empty());
        Mockito.when(walletRepository.findByIdForUpdate(TEST_WALLET_ID)).thenReturn(Optional.of(wallet));

        TransactionResponseDTO response = walletService.processTransaction(dto);

        assertNotNull(response);
        assertEquals(StatusEnum.FAILED, response.status());
        assertEquals(new BigDecimal("50.00"), wallet.getBalance()); // Balance remains unchanged

        ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementRepository).save(movementCaptor.capture());
        assertEquals(StatusEnum.FAILED, movementCaptor.getValue().getStatus());
        assertTrue(movementCaptor.getValue().getDescriptionCounterpart().contains("Insufficient funds"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting an unsupported transaction type")
    void shouldThrowExceptionForUnsupportedType() {

        var dto = new TransactionRequestDTO(
                TEST_TRANSFER_ID,
                TEST_WALLET_ID,
                TransactionTypeEnum.DEPOSIT,
                new BigDecimal("100.00"),
                new CounterpartyRequestDTO(TEST_COUNTERPARTY_ID));

        var wallet = Wallet.builder().id(TEST_WALLET_ID).balance(new BigDecimal("100.00")).build();

        Mockito.when(movementRepository.findByTransferId(TEST_TRANSFER_ID)).thenReturn(Optional.empty());
        Mockito.when(walletRepository.findByIdForUpdate(TEST_WALLET_ID)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () -> walletService.processTransaction(dto));
    }

    @Test
    @DisplayName("Should return mapped list of wallets for given client and account criteria")
    void shouldFindAndMapWallets() {

        var mockWallet = Wallet.builder().id(TEST_WALLET_ID).name("Main Wallet")
                .balance(BigDecimal.TEN).clientId(TEST_CLIENT_ID).accountId(TEST_ACCOUNT_ID).build();

        Mockito.when(walletRepository.findByClientIdAndAccountId(TEST_CLIENT_ID, TEST_ACCOUNT_ID)).thenReturn(List.of(mockWallet));

        List<WalletResponseDTO> result = walletService.findWallets(TEST_CLIENT_ID, TEST_ACCOUNT_ID);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Main Wallet", result.getFirst().name());
    }

    @Test
    @DisplayName("Should return existing wallet instead of recreating if client already has a wallet registered")
    void shouldReturnExistingMainWallet() {
        var mockWallet = Wallet.builder().id(TEST_WALLET_ID).name("Existing Wallet")
                .balance(BigDecimal.ZERO).clientId(TEST_CLIENT_ID).accountId(TEST_ACCOUNT_ID).build();

        Mockito.when(walletRepository.findByClientIdAndAccountId(TEST_CLIENT_ID, TEST_ACCOUNT_ID)).thenReturn(List.of(mockWallet));

        WalletResponseDTO response = walletService.initializeOrGetMainWallet(TEST_CLIENT_ID, TEST_ACCOUNT_ID);

        assertNotNull(response);
        assertEquals("Existing Wallet", response.name());
        Mockito.verify(walletRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Should provision and auto-save a new Main Wallet if no entries exist for the account context")
    void shouldProvisionNewMainWallet() {
        Mockito.when(walletRepository.findByClientIdAndAccountId(TEST_CLIENT_ID, TEST_ACCOUNT_ID)).thenReturn(Collections.emptyList());
        Mockito.when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponseDTO response = walletService.initializeOrGetMainWallet(TEST_CLIENT_ID, TEST_ACCOUNT_ID);

        assertNotNull(response);
        assertEquals("Main Wallet", response.name());
        assertEquals(BigDecimal.ZERO, response.balance());
        Mockito.verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should return live database snapshot balance when target date is null")
    void shouldReturnLiveCurrentBalanceWhenDateIsNull() {

        var wallet = Wallet.builder().id(TEST_WALLET_ID).balance(new BigDecimal("1250.00")).build();
        Mockito.when(walletRepository.findById(TEST_WALLET_ID)).thenReturn(Optional.of(wallet));

        WalletBalanceResponseDTO response = walletService.getBalanceAtDate(TEST_WALLET_ID, null);

        assertNotNull(response);
        assertEquals(new BigDecimal("1250.00"), response.balance());
    }

    @Test
    @DisplayName("Should execute reverse arithmetic calculation to rebuild historical ledger balance accurately")
    void shouldCalculatePastHistoricalBalanceCorrectly() {
        var wallet = Wallet.builder().id(TEST_WALLET_ID).balance(new BigDecimal("1000.00")).build();
        var targetPastDate = OffsetDateTime.now().minusDays(5);

        var m1 = Movement.builder().type(MovementTypeEnum.INCOMING).amount(new BigDecimal("200.00")).build();
        var m2 = Movement.builder().type(MovementTypeEnum.OUTGOING).amount(new BigDecimal("50.00")).build();

        Mockito.when(walletRepository.findById(TEST_WALLET_ID)).thenReturn(Optional.of(wallet));
        Mockito.when(movementRepository.findByWalletIdAndStatusAndDateTimeMovementAfter(eq(TEST_WALLET_ID), eq(StatusEnum.COMPLETED), any()))
                .thenReturn(List.of(m1, m2));

        WalletBalanceResponseDTO response = walletService.getBalanceAtDate(TEST_WALLET_ID, targetPastDate);

        assertNotNull(response);
        assertEquals(new BigDecimal("850.00"), response.balance());
    }
}