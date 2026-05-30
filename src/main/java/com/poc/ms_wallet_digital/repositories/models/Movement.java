package com.poc.ms_wallet_digital.repositories.models;

import com.poc.ms_wallet_digital.enums.MovementTypeEnum;
import com.poc.ms_wallet_digital.enums.StatusEnum;
import com.poc.ms_wallet_digital.enums.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_movement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transfer_id", nullable = false, unique = true)
    private String transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementTypeEnum type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionTypeEnum operation;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private String descriptionCounterpart;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateTimeMovement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEnum status;

    @PrePersist
    protected void onCreate() {
        if (this.dateTimeMovement == null) {
            this.dateTimeMovement = OffsetDateTime.now();
        }
    }
}