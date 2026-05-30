package com.poc.ms_wallet_digital.repositories;

import com.poc.ms_wallet_digital.enums.MovementTypeEnum;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;
import com.poc.ms_wallet_digital.repositories.models.Movement;
import com.poc.ms_wallet_digital.repositories.models.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class MovementRepositoryTest {

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private WalletRepository walletRepository;


    @Test
    @DisplayName("Should find movement successfully using the external transferId token")
    void shouldFindMovementByTransferId() {

        Wallet wallet = walletRepository.save(
                Wallet.builder().name("Main").balance(BigDecimal.ZERO)
                        .clientId(UUID.randomUUID()).accountId(UUID.randomUUID()).build());

        Movement movement = Movement.builder()
                .transferId("tx-unique-999")
                .wallet(wallet)
                .type(MovementTypeEnum.INCOMING)
                .operation(TransactionTypeEnum.DEPOSIT)
                .amount(BigDecimal.TEN)
                .status(StatusEnum.COMPLETED)
                .build();
        movementRepository.save(movement);

        Optional<Movement> found = movementRepository.findByTransferId("tx-unique-999");

        assertTrue(found.isPresent());
        assertEquals("tx-unique-999", found.get().getTransferId());
    }

    @Test
    @DisplayName("Should fetch historical movements that were recorded strictly after a specific threshold date")
    void shouldFindHistoricalMovementsAfterDate() {
        // Arrange
        Wallet wallet = walletRepository.save(
                Wallet.builder().name("Main").balance(BigDecimal.ZERO)
                        .clientId(UUID.randomUUID()).accountId(UUID.randomUUID()).build());
        OffsetDateTime now = OffsetDateTime.now();

        Movement oldMovement = Movement.builder()
                .transferId("tx-old")
                .wallet(wallet)
                .type(MovementTypeEnum.INCOMING)
                .operation(TransactionTypeEnum.DEPOSIT)
                .amount(BigDecimal.ONE)
                .status(StatusEnum.COMPLETED)
                .dateTimeMovement(now.minusDays(10))
                .build();

        Movement newMovement = Movement.builder()
                .transferId("tx-new")
                .wallet(wallet)
                .type(MovementTypeEnum.OUTGOING)
                .operation(TransactionTypeEnum.WITHDRAW)
                .amount(BigDecimal.ONE)
                .status(StatusEnum.COMPLETED)
                .dateTimeMovement(now.minusMinutes(5))
                .build();

        movementRepository.saveAll(List.of(oldMovement, newMovement));

        List<Movement> results = movementRepository.findByWalletIdAndStatusAndDateTimeMovementAfter(
                wallet.getId(),
                StatusEnum.COMPLETED,
                now.minusDays(2)
        );

        assertEquals(1, results.size());
        assertEquals("tx-new", results.getFirst().getTransferId());
    }
}