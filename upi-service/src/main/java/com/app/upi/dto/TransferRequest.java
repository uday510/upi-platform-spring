package com.app.upi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    private String toUpi;
    private BigDecimal amount;

    private String idempotencyKey;

}
