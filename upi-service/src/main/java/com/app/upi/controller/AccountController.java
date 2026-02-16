package com.app.upi.controller;

import com.app.upi.dto.AccountResponse;
import com.app.upi.dto.CreateAccountRequest;
import com.app.upi.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/upi/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @RequestHeader("X-User") String authenticatedUserId
    ) {
        log.info("REST request to create account for UPI: {} by User: {}",
                request.getUpiId(), authenticatedUserId);

        AccountResponse response = accountService.createAccount(request, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{upiId:.+}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String upiId,
            @RequestHeader("X-User") String authenticatedUser
    ) {
        log.info("User {} requesting account for {}", authenticatedUser, upiId);

        if (!authenticatedUser.equalsIgnoreCase(upiId)) {
            log.warn("Security Alert! User {} tried to access account {}", authenticatedUser, upiId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AccountResponse response = accountService.getAccountSecurely(upiId, authenticatedUser);
        return ResponseEntity.ok(response);
    }

}