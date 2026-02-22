package com.app.upi.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferCompletedEvent {

    private UUID transactionId;

    private UUID fromAccountId;
    private UUID toAccountId;

    private BigDecimal amount;

    private UUID userId;

    private Instant completedAt;

}
