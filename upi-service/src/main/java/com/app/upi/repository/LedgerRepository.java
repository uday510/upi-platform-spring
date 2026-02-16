package com.app.upi.repository;

import com.app.upi.entity.LedgerEntry;
import com.app.upi.enums.EntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, UUID> {

    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(
            UUID accountId,
            Pageable pageable
    );


    List<LedgerEntry> findByTransactionId(UUID transactionId);

    @Query("""
        SELECT COALESCE(
            SUM(
                CASE
                    WHEN l.type = 'CREDIT' THEN l.amount
                    ELSE -l.amount
                END
            ), 0
        )
        FROM LedgerEntry l
        WHERE l.accountId = :accountId
    """)
    BigDecimal calculateBalance(UUID accountId);


    @Query("""
        SELECT COALESCE(SUM(l.amount), 0)
        FROM LedgerEntry l
        WHERE l.type = :type
          AND l.createdAt BETWEEN :from AND :to
    """)
    BigDecimal sumByTypeAndPeriod(
            EntryType type,
            Instant from,
            Instant to
    );
}