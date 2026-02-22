package com.app.upi.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceResponse {

    private UUID accountId;

    private String upiId;

    private BigDecimal balance;

}
