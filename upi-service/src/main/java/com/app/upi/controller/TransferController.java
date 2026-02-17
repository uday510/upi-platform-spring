package com.app.upi.controller;

import com.app.upi.dto.TransferRequest;
import com.app.upi.dto.TransferResponse;
import com.app.upi.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upi/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader(value = "X-Role", required = false) String role,
            @Valid @RequestBody TransferRequest request
    ) {

        if (role != null && !role.equalsIgnoreCase("USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID txId = transferService.transfer(request, userId);

        TransferResponse response = TransferResponse.builder()
                .transactionId(txId)
                .status(HttpStatus.OK.getReasonPhrase())
                .message("Transfer completed successfully")
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}