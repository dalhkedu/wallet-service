package com.poc.ms_wallet_digital.repositories;

import com.poc.ms_wallet_digital.repositories.models.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Wallet c WHERE c.id = :id")
    Optional<Wallet> findByIdForUpdate(UUID id);

    List<Wallet> findByClientIdAndAccountId(UUID clientId, UUID accountId);

}