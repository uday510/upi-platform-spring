package com.app.notificationservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransferCompletedEvent {

    private UUID transactionId;

    private UUID fromAccountId;

    private UUID toAccountId;

    private UUID userId;

    private BigDecimal amount;

    private Instant completedAt;

}
