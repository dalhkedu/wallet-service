package com.poc.ms_wallet_digital.repositories;


import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.repositories.models.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovementRepository extends JpaRepository<Movement, UUID> {

    List<Movement> findByWalletIdAndStatusAndDateTimeMovementAfter(UUID walletId, StatusEnum status, OffsetDateTime date);

    Optional<Movement> findByTransferId(String transferId);
}
