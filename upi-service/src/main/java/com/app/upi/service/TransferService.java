package com.app.upi.service;

import com.app.upi.dto.TransferRequest;
import com.app.upi.entity.Account;
import com.app.upi.entity.LedgerEntry;
import com.app.upi.entity.Transaction;
import com.app.upi.enums.EntryType;
import com.app.upi.exception.AccountException;
import com.app.upi.exception.TransferException;
import com.app.upi.repository.AccountRepository;
import com.app.upi.repository.LedgerRepository;
import com.app.upi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;


    @Transactional
    public UUID transfer(TransferRequest request, String senderUpi) {

        validateRequest(request, senderUpi);

        transactionRepository.findByIdempotencyKey(
                request.getIdempotencyKey()
        ).ifPresent(tx -> {
            throw new AccountException("Duplicate Transaction");
        });

        Account fromAcc = accountRepository.findByUpiId(senderUpi)
                .orElseThrow(() ->
                        new TransferException("Sender not found"));

        Account toAcc = accountRepository.findByUpiId(request.getToUpi())
                .orElseThrow(() ->
                        new TransferException("Receiver not found"));

        if (fromAcc.equals(toAcc)) {
            throw new TransferException("Cannot transfer to self");
        }

        UUID firstId = fromAcc.getId()
                .compareTo(toAcc.getId()) < 0
                ? fromAcc.getId()
                : toAcc.getId();

        UUID secondId = firstId.equals(fromAcc.getId())
                ? toAcc.getId()
                : fromAcc.getId();

        Account first = accountRepository
                .findByIdForUpdate(firstId)
                .orElseThrow();

        Account second = accountRepository
                .findByIdForUpdate(secondId)
                .orElseThrow();

        Account from = first.equals(fromAcc) ? first : second;
        Account to = first.equals(toAcc) ? first : second;

        from.debit(request.getAmount());
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

            return transactionRepository
                    .findByIdempotencyKey(
                            request.getIdempotencyKey()
                    )
                    .map(Transaction::getId)
                    .orElseThrow(() ->
                            new TransferException("Duplicate transfer"));
        }

        accountRepository.saveAll(List.of(from, to));

        // Ledger (append-only)
        ledgerRepository.save(
                LedgerEntry.create(
                        from.getId(),
                        tx.getId(),
                        EntryType.DEBIT,
                        request.getAmount()
                )
        );

        ledgerRepository.save(
                LedgerEntry.create(
                        to.getId(),
                        tx.getId(),
                        EntryType.CREDIT,
                        request.getAmount()
                )
        );

        tx.markSuccess();
        transactionRepository.save(tx);

        return tx.getId();
    }



    private void validateRequest(
            TransferRequest request,
            String senderUpi
    ) {

        if (request == null) {
            throw new TransferException("Request cannot be null");
        }

        if (senderUpi == null || senderUpi.isBlank()) {
            throw new TransferException("Sender UPI required");
        }

        if (request.getToUpi() == null ||
                request.getToUpi().isBlank()) {

            throw new TransferException("Receiver UPI required");
        }

        if (request.getIdempotencyKey() == null ||
                request.getIdempotencyKey().isBlank()) {

            throw new TransferException("Idempotency key required");
        }

        if (request.getAmount() == null ||
                request.getAmount().signum() <= 0) {

            throw new TransferException("Invalid amount");
        }
    }
}