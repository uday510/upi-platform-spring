package com.app.upi.service;

import com.app.upi.dto.AccountResponse;
import com.app.upi.dto.CreateAccountRequest;
import com.app.upi.entity.Account;
import com.app.upi.exception.AccountException;
import com.app.upi.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Creates a new account linked to a specific User ID.
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String authenticatedUserId) {
        validateRequest(request);

        UUID userId = UUID.fromString(authenticatedUserId);
        String upi = normalizeUpi(request.getUpiId());

        log.info("Creating account for User: {} with UPI: {}", userId, upi);

        if (accountRepository.findByUpiId(upi).isPresent()) {
            throw new AccountException("UPI ID already exists");
        }

        Account account = Account.create(
                userId,
                upi,
                request.getInitialBalance()
        );

        try {
            Account savedAccount = accountRepository.saveAndFlush(account);
            return mapToResponse(savedAccount);

        } catch (DataIntegrityViolationException ex) {
            log.error("Conflict: UPI ID {} race condition hit", upi);
            throw new AccountException("UPI ID already exists", ex);
        }
    }


    @Transactional(readOnly = true)
    public AccountResponse getAccountSecurely(String upiId, String authenticatedUserId) {
        UUID requesterId = UUID.fromString(authenticatedUserId);
        String upi = normalizeUpi(upiId);

        Account account = accountRepository.findByUpiId(upi)
                .orElseThrow(() -> new AccountException("Account not found"));

        if (!account.getUserId().equals(requesterId)) {
            log.warn("Unauthorized access: User {} tried to view UPI {}", requesterId, upi);
            throw new AccountException("Access Denied: You do not own this account");
        }

        return mapToResponse(account);
    }

    private void validateRequest(CreateAccountRequest request) {
        if (request == null) throw new AccountException("Request cannot be null");
        if (request.getUpiId() == null || request.getUpiId().isBlank()) {
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