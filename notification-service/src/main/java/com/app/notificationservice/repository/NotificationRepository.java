package com.app.notificationservice.repository;

import com.app.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    boolean existsByTransactionId(UUID transactionId);
}