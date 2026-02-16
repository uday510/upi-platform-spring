package com.app.upi.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_upi",
                        columnNames = "upi_id"
                )
        }
)
@Check(constraints = "balance >= 0")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {


    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "upi_id",
            nullable = false,
            unique = true,
            length = 100,
            updatable = false
    )
    private String upiId;


    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Version
    private Long version;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Account create(String upiId, BigDecimal initialBalance) {

        if (upiId == null || upiId.isBlank()) {
            throw new IllegalArgumentException("UPI ID is required");
        }

        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Initial balance cannot be negative"
            );
        }

        Account account = new Account();
        account.id = UUID.randomUUID();
        account.upiId = upiId.trim().toLowerCase();
        account.balance = initialBalance;

        return account;
    }


    public void debit(BigDecimal amount) {

        validateAmount(amount);

        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        this.balance = this.balance.subtract(amount);
    }


    public void credit(BigDecimal amount) {

        validateAmount(amount);

        this.balance = this.balance.add(amount);
    }


    private void validateAmount(BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }



    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (!(o instanceof Account other)) return false;

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}