package com.app.upi.controller;

import com.app.upi.dto.BalanceResponse;
import com.app.upi.dto.TransferRequest;
import com.app.upi.dto.TransferResponse;
import com.app.upi.entity.Transaction;
import com.app.upi.service.AccountService;
import com.app.upi.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upi")
@RequiredArgsConstructor
public class UpiController {

    private final TransferService transferService;
    private final AccountService accountService;

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("X-User") UUID userId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @Valid @RequestBody TransferRequest request
    ) {

        if (role != null && !role.equalsIgnoreCase("ROLE_USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Transaction tx = transferService.transferAndGet(request, userId);
        TransferResponse response =
                TransferResponse.builder()
                        .transactionId(tx.getId())
                        .status(tx.getStatus().name())

                        .fromUpi(request.getFromUpi())
                        .toUpi(request.getToUpi())

                        .amount(tx.getAmount())
                        .createdAt(tx.getCreatedAt())

                        .message(HttpStatus.OK.getReasonPhrase())
                        .build();


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping({"/balance", "/balance/"})
    public ResponseEntity<BalanceResponse> getBalance(
            @RequestHeader("X-User") String userId
    ) {

        BalanceResponse response =
                accountService.getBalance(userId);

        return ResponseEntity.ok(response);
    }

}