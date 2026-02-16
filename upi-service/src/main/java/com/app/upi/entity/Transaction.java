package com.app.upi.entity;

import com.app.upi.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_from", columnList = "from_account_id"),
                @Index(name = "idx_tx_to", columnList = "to_account_id"),
                @Index(name = "idx_tx_idempotency", columnList = "idempotency_key", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private UUID toAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            insertable = false
    )
    private LocalDateTime createdAt;
}