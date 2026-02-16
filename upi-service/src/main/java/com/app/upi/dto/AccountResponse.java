package com.app.upi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {
    private UUID id;
    private String upiId;
    private BigDecimal balance;
    private Instant createdAt;;

}
