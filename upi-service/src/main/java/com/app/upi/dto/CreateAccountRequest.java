package com.app.upi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotBlank(message = "UPI ID is mandatory")
    private String upiId;


    @JsonProperty("initialBalance")
    @PositiveOrZero(message = "Balance cannot be negative")
    private BigDecimal initialBalance;
}