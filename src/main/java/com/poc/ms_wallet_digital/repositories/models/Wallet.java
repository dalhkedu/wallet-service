package com.poc.ms_wallet_digital.repositories.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "pessoa_id", nullable = false)
    private UUID clientId;

    @Column(name = "conta_id", nullable = false)
    private UUID accountId;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime atDateTimeCreation;

    @PrePersist
    protected void onCreate() {
        this.atDateTimeCreation = OffsetDateTime.now();
        if (this.balance == null) this.balance = BigDecimal.ZERO;
    }
}
