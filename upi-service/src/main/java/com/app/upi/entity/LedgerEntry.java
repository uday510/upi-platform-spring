package com.app.upi.entity;

import com.app.upi.enums.EntryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_account", columnList = "account_id"),
                @Index(name = "idx_ledger_tx", columnList = "transaction_id"),
                @Index(name = "idx_ledger_created", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry {

    /* =================================================
       Identity
       ================================================= */

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private EntryType type;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    public static LedgerEntry create(
            UUID accountId,
            UUID transactionId,
            EntryType type,
            BigDecimal amount
    ) {

        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }

        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Entry type cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        LedgerEntry entry = new LedgerEntry();
        entry.id = UUID.randomUUID();
        entry.accountId = accountId;
        entry.transactionId = transactionId;
        entry.type = type;
        entry.amount = amount;

        return entry;
    }
}