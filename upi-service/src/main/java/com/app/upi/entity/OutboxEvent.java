package com.app.upi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_status", columnList = "status"),
                @Index(name = "idx_outbox_next_retry", columnList = "next_retry_at")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status; // PENDING / SENT / FAILED

    @Column(nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error")
    private String lastError;

    @CreationTimestamp
    private Instant createdAt;

    /* ================================
       Domain Methods
       ================================ */

    public void markSent() {
        this.status = "SENT";
        this.lastError = null;
        this.nextRetryAt = null;
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }

    public void incrementRetry(String error, int maxRetries) {

        this.retryCount++;
        this.lastError = error;

        if (this.retryCount >= maxRetries) {
            markFailed();
            return;
        }

        // Exponential backoff: 30s, 60s, 120s, 240s...
        long delay = (long) Math.pow(2, retryCount) * 30;

        this.nextRetryAt =
                Instant.now().plusSeconds(delay);
    }
}