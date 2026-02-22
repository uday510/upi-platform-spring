package com.app.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_logs",
        indexes = {
                @Index(name = "idx_log_notification", columnList = "notification_id"),
                @Index(name = "idx_log_status", columnList = "status"),
                @Index(name = "idx_log_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(nullable = false)
    private String status; // ATTEMPT / SUCCESS / FAIL

    @Column(length = 2000)
    private String errorMessage;

    private Long kafkaOffset;

    private Integer partitionId;

    @CreationTimestamp
    private Instant createdAt;
}