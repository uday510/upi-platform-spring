package com.app.upi.entity;

import com.app.upi.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_from_created", columnList = "from_account_id, created_at"),
                @Index(name = "idx_tx_to_created", columnList = "to_account_id, created_at"),
                @Index(name = "idx_tx_idem", columnList = "idempotency_key", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "from_account_id", nullable = false, updatable = false)
    private UUID fromAccountId;

    @Column(name = "to_account_id", nullable = false, updatable = false)
    private UUID toAccountId;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true, updatable = false, length = 64)
    private String idempotencyKey;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    public static Transaction create(
            UUID fromAccountId,
            UUID toAccountId,
            BigDecimal amount,
            String idempotencyKey
    ) {

        Transaction tx = new Transaction();
        tx.id = UUID.randomUUID();
        tx.fromAccountId = fromAccountId;
        tx.toAccountId = toAccountId;
        tx.amount = amount;
        tx.status = TransactionStatus.PENDING;
        tx.idempotencyKey = idempotencyKey;
        return tx;
    }


    public void markSuccess() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction not in PENDING state");
        }
        this.status = TransactionStatus.SUCCESS;
    }

    public void markFailed() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction not in PENDING state");
        }
        this.status = TransactionStatus.FAILED;
    }


    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}