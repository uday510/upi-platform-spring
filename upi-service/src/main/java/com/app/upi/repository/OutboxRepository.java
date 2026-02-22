package com.app.upi.repository;

import com.app.upi.entity.OutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o FROM OutboxEvent o
            WHERE o.status = 'PENDING'
            AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= CURRENT_TIMESTAMP)
            ORDER BY o.createdAt
            """)
    List<OutboxEvent> findForPublishing(Pageable pageable);

}