package com.app.upi.controller;

import com.app.upi.dto.AccountResponse;
import com.app.upi.dto.CreateAccountRequest;
import com.app.upi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/upi/")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public AccountResponse createAccount(
            @RequestBody CreateAccountRequest request
            ) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{upiId}")
    public AccountResponse getAccount(
            @PathVariable String upiId
    ) {
        return accountService.getAccount(upiId);
    }
}
