package com.app.upi.service;

import com.app.upi.dto.TransferRequest;
import com.app.upi.entity.Account;
import com.app.upi.entity.LedgerEntry;
import com.app.upi.entity.OutboxEvent;
import com.app.upi.entity.Transaction;
import com.app.upi.enums.EntryType;
import com.app.upi.event.TransferCompletedEvent;
import com.app.upi.exception.TransferEventProducer;
import com.app.upi.exception.TransferException;
import com.app.upi.repository.AccountRepository;
import com.app.upi.repository.LedgerRepository;
import com.app.upi.repository.OutboxRepository;
import com.app.upi.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final TransferEventProducer transferEventProducer;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    @Transactional
    public Transaction transferAndGet(TransferRequest request, UUID userId) {

        log.info("Transfer request | from={} to={} amount={} key={}",
                request.getFromUpi(),
                request.getToUpi(),
                request.getAmount(),
                request.getIdempotencyKey());


        validateRequest(request, request.getFromUpi());

        // 1 Idempotency
        var existingTx =
                transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existingTx.isPresent()) {

            log.warn("Duplicate transaction detected for key: {}. Returning existing ID: {}",
                    request.getIdempotencyKey(), existingTx.get().getId());

            return existingTx.get();
        }

        // 2 Sender
        Account fromAcc = accountRepository
                .findByUpiId(request.getFromUpi())
                .orElseThrow(() ->
                        new TransferException("Sender account not found",
                                HttpStatus.NOT_FOUND)
                );

        if (!userId.equals(fromAcc.getUserId())) {
            throw new TransferException(
                    "Unauthorized account access",
                    HttpStatus.FORBIDDEN
            );
        }

        // 3 Receiver
        Account toAcc = accountRepository
                .findByUpiId(request.getToUpi())
                .orElseThrow(() ->
                        new TransferException("Receiver account not found",
                                HttpStatus.NOT_FOUND)
                );

        if (fromAcc.equals(toAcc)) {
            throw new TransferException(
                    "Cannot transfer to same account",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 4. Lock ordering (deadlock prevention)
        UUID firstId = fromAcc.getId().compareTo(toAcc.getId()) < 0
                ? fromAcc.getId()
                : toAcc.getId();

        UUID secondId = firstId.equals(fromAcc.getId())
                ? toAcc.getId()
                : fromAcc.getId();


        Account first =
                accountRepository.findByIdForUpdate(firstId)
                        .orElseThrow(() ->
                                new TransferException("Account lock failed",
                                        HttpStatus.CONFLICT)
                        );

        Account second =
                accountRepository.findByIdForUpdate(secondId)
                        .orElseThrow(() ->
                                new TransferException("Account lock failed",
                                        HttpStatus.CONFLICT)
                        );

        Account from = first.equals(fromAcc) ? first : second;
        Account to = first.equals(toAcc) ? first : second;

        try {
            from.debit(request.getAmount());
        } catch (IllegalStateException ex) {
            throw new TransferException(
                    "Insufficient balance",
                    HttpStatus.BAD_REQUEST
            );
        }

        to.credit(request.getAmount());

        Transaction tx = Transaction.create(
                from.getId(),
                to.getId(),
                request.getAmount(),
                request.getIdempotencyKey()
        );

        try {
            transactionRepository.save(tx);
        } catch (DataIntegrityViolationException ex) {

            log.warn("Idempotency conflict: {}", request.getIdempotencyKey());

            return transactionRepository
                    .findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() ->
                                    new TransferException(
                            "Duplicate transaction",
                            HttpStatus.CONFLICT
                                    )
                    );
        }

        accountRepository.saveAll(List.of(from, to));

        ledgerRepository.save(
                LedgerEntry.
                        create(from.getId(),
                                tx.getId(),
                                EntryType.DEBIT,
                                request.getAmount())
        );

        ledgerRepository.save(
                LedgerEntry.
                        create(to.getId(),
                                tx.getId(),
                                EntryType.CREDIT,
                                request.getAmount()
                        )
        );

        tx.markSuccess();
        transactionRepository.save(tx);

        TransferCompletedEvent transferCompletedEvent =
                TransferCompletedEvent.builder()
                        .transactionId(tx.getId())
                        .fromAccountId(tx.getFromAccountId())
                        .toAccountId(tx.getToAccountId())
                        .amount(request.getAmount())
                        .userId(userId)
                        .completedAt(Instant.now())
                        .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType("TRANSACTION")
                .aggregateId(tx.getId())
                .eventType("TRANSFER_COMPLETED")
                .payload(toJson(transferCompletedEvent))
                .status("PENDING")
                .build();


        outboxRepository.save(outboxEvent);

        log.info("Transfer success | txId={}", tx.getId());
        return tx;
    }

    private void validateRequest(TransferRequest request, String senderUpi) {
        if (request == null || senderUpi == null || senderUpi.isBlank() ||
                request.getToUpi() == null || request.getIdempotencyKey() == null ||
                request.getAmount() == null || request.getAmount().signum() <= 0) {

            log.error("Validation failed for transfer request from: {}", senderUpi);
            throw new TransferException("Invalid transfer request parameters");
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new TransferException("Event serialization failed");
        }
    }

}