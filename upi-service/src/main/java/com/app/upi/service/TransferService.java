package com.app.upi.service;

import com.app.upi.dto.TransferRequest;
import com.app.upi.entity.Account;
import com.app.upi.entity.LedgerEntry;
import com.app.upi.entity.Transaction;
import com.app.upi.enums.EntryType;
import com.app.upi.exception.TransferException;
import com.app.upi.repository.AccountRepository;
import com.app.upi.repository.LedgerRepository;
import com.app.upi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    @Transactional
    public UUID transfer(TransferRequest request, UUID userId) {
        log.info("Initiating transfer request. From: {}, To: {}, Amount: {}, Key: {}",
                request.getFromUpi(), request.getToUpi(), request.getAmount(), request.getIdempotencyKey());

        validateRequest(request, request.getFromUpi());

        var existingTx = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingTx.isPresent()) {
            log.warn("Duplicate transaction detected for key: {}. Returning existing ID: {}",
                    request.getIdempotencyKey(), existingTx.get().getId());
            return existingTx.get().getId();
        }

        Account fromAcc = accountRepository.findByUpiId(request.getFromUpi())
                .orElseThrow(() -> {
                    log.error("Transfer failed: Sender {} not found", request.getFromUpi());
                    return new TransferException("Sender not found");
                });

        if (!userId.equals(fromAcc.getUserId())) {
            log.error("Unauthorized transfer attempt: User {} tried to access account of User {}",
                    userId, fromAcc.getUserId());
            throw new TransferException("Unauthorized access to account");
        }

        Account toAcc = accountRepository.findByUpiId(request.getToUpi())
                .orElseThrow(() -> {
                    log.error("Transfer failed: Receiver {} not found", request.getToUpi());
                    return new TransferException("Receiver not found");
                });

        if (fromAcc.equals(toAcc)) {
            log.error("Transfer failed: Attempted self-transfer for UPI: {}", request.getFromUpi());
            throw new TransferException("Cannot transfer to self");
        }

        UUID firstId = fromAcc.getId().compareTo(toAcc.getId()) < 0 ? fromAcc.getId() : toAcc.getId();
        UUID secondId = firstId.equals(fromAcc.getId()) ? toAcc.getId() : fromAcc.getId();

        log.debug("Acquiring locks in order: {} -> {}", firstId, secondId);
        Account first = accountRepository.findByIdForUpdate(firstId).orElseThrow();
        Account second = accountRepository.findByIdForUpdate(secondId).orElseThrow();

        Account from = first.equals(fromAcc) ? first : second;
        Account to = first.equals(toAcc) ? first : second;

        log.debug("Updating balances. From Account: {}, To Account: {}", from.getId(), to.getId());
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
            log.warn("Race condition: Data integrity violation for key {}. Fetching existing record.",
                    request.getIdempotencyKey());
            return transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .map(Transaction::getId)
                    .orElseThrow(() -> new TransferException("Concurrent transfer conflict"));
        }

        accountRepository.saveAll(List.of(from, to));

        ledgerRepository.save(LedgerEntry.create(from.getId(), tx.getId(), EntryType.DEBIT, request.getAmount()));
        ledgerRepository.save(LedgerEntry.create(to.getId(), tx.getId(), EntryType.CREDIT, request.getAmount()));

        tx.markSuccess();
        transactionRepository.save(tx);

        log.info("Transfer completed successfully. Transaction ID: {}, Key: {}", tx.getId(), request.getIdempotencyKey());
        return tx.getId();
    }

    private void validateRequest(TransferRequest request, String senderUpi) {
        if (request == null || senderUpi == null || senderUpi.isBlank() ||
                request.getToUpi() == null || request.getIdempotencyKey() == null ||
                request.getAmount() == null || request.getAmount().signum() <= 0) {

            log.error("Validation failed for transfer request from: {}", senderUpi);
            throw new TransferException("Invalid transfer request parameters");
        }
    }

}