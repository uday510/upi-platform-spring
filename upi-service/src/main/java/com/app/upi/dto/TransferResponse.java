package com.app.upi.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TransferResponse {

    private UUID transactionId;

    private String status;

    private String fromUpi;

    private String toUpi;

    private BigDecimal amount;

    private Instant createdAt;

    private String message;
}