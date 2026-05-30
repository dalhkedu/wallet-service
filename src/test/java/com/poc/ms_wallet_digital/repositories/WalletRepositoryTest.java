package com.poc.ms_wallet_digital.repositories;

import com.poc.ms_wallet_digital.repositories.models.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @DisplayName("Should successfully find a wallet and apply pessimistic write lock")
    void shouldFindWalletWithPessimisticLock() {

        Wallet wallet = Wallet.builder()
                .name("Savings Wallet")
                .balance(new BigDecimal("500.00"))
                .clientId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        Optional<Wallet> foundWallet = walletRepository.findByIdForUpdate(savedWallet.getId());

        assertTrue(foundWallet.isPresent());
        assertEquals(savedWallet.getId(), foundWallet.get().getId());
        assertEquals(new BigDecimal("500.00"), foundWallet.get().getBalance());
    }

    @Test
    @DisplayName("Should return wallets matching exact client and account identity criteria")
    void shouldFindWalletsByClientAndAccount() {

        UUID clientId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Wallet wallet1 = Wallet.builder().name("Wallet 1").balance(BigDecimal.ZERO).clientId(clientId).accountId(accountId).build();
        Wallet wallet2 = Wallet.builder().name("Wallet 2").balance(BigDecimal.ZERO).clientId(clientId).accountId(accountId).build();
        Wallet walletWithDifferentClient = Wallet.builder().name("Wallet 3").balance(BigDecimal.ZERO).clientId(UUID.randomUUID()).accountId(accountId).build();

        walletRepository.saveAll(List.of(wallet1, wallet2, walletWithDifferentClient));

        List<Wallet> results = walletRepository.findByClientIdAndAccountId(clientId, accountId);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(w -> w.getName().equals("Wallet 1")));
        assertTrue(results.stream().anyMatch(w -> w.getName().equals("Wallet 2")));
    }
}