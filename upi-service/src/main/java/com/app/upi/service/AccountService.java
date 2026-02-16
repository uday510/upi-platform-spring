package com.app.upi.service;

import com.app.upi.dto.AccountResponse;
import com.app.upi.dto.CreateAccountRequest;
import com.app.upi.entity.Account;
import com.app.upi.exception.AccountException;
import com.app.upi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {

        validateRequest(request);

        String upi = normalizeUpi(request.getUpiId());

        return accountRepository.findByUpiId(upi)
                .map(this::mapToResponse)
                .orElseGet(() -> createNewAccount(upi, request));
    }


    private AccountResponse createNewAccount(
            String upi,
            CreateAccountRequest request
    ) {

        Account account = Account.create(
                upi,
                request.getInitialBalance()
        );

        try {
            return mapToResponse(accountRepository.save(account));

        } catch (DataIntegrityViolationException ex) {

            return accountRepository.findByUpiId(upi)
                    .map(this::mapToResponse)
                    .orElseThrow(() ->
                            new AccountException("UPI ID already exists"));
        }
    }


    @Transactional(readOnly = true)
    public AccountResponse getAccount(String upiId) {

        String upi = normalizeUpi(upiId);

        Account account = accountRepository.findByUpiId(upi)
                .orElseThrow(() ->
                        new AccountException("Account not found"));

        return mapToResponse(account);
    }



    private void validateRequest(CreateAccountRequest request) {

        if (request == null) {
            throw new AccountException("Request cannot be null");
        }

        if (request.getUpiId() == null ||
                request.getUpiId().isBlank()) {

            throw new AccountException("UPI ID is required");
        }
    }


    private String normalizeUpi(String upi) {
        return upi.trim().toLowerCase();
    }


    private AccountResponse mapToResponse(Account account) {

        return AccountResponse.builder()
                .id(account.getId())
                .upiId(account.getUpiId())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }
}