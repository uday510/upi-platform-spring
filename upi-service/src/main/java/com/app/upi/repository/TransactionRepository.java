package com.app.upi.repository;

import com.app.upi.entity.Transaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {


    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.id = :id")
    @QueryHints({
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "3000"
            )
    })
    Optional<Transaction> findByIdForUpdate(UUID id);


    @Query("""
        SELECT t FROM Transaction t
        WHERE t.fromAccountId = :accountId
           OR t.toAccountId = :accountId
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findAccountHistory(
            UUID accountId,
            Pageable pageable
    );
}