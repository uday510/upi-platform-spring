package com.app.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notify_user", columnList = "user_id"),
                @Index(name = "idx_notify_tx", columnList = "transaction_id", unique = true),
                @Index(name = "idx_notify_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(nullable = false, length = 20)
    private String channel; // EMAIL, SMS, PUSH

    @Column(nullable = false, length = 20)
    private String status; // PENDING, SENT, FAILED

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 1000)
    private String lastError;

    @CreationTimestamp
    private Instant createdAt;

    private Instant sentAt;
}