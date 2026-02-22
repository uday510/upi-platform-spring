package com.app.notificationservice.service;

import com.app.notificationservice.dto.TransferCompletedEvent;
import com.app.notificationservice.entity.Notification;
import com.app.notificationservice.entity.NotificationLog;
import com.app.notificationservice.repository.NotificationLogRepository;
import com.app.notificationservice.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferCompletedConsumer {

    private final NotificationRepository notificationRepo;
    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;
    private final ObjectMapper mapper;

    @KafkaListener(
            topics = "transfer.completed",
            groupId = "notification-group"
    )
    public void consume(
            String message,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {

        Notification notification = null;

        try {

            TransferCompletedEvent event =
                    mapper.readValue(message, TransferCompletedEvent.class);

            if (notificationRepo.existsByTransactionId(event.getTransactionId())) {

                log.warn("Duplicate ignored tx={}", event.getTransactionId());
                return;
            }

            notification =
                    Notification.builder()
                            .userId(event.getUserId())
                            .transactionId(event.getTransactionId())
                            .channel("EMAIL")
                            .status("PENDING")
                            .retryCount(0)
                            .build();

            notificationRepo.save(notification);


            notificationLogRepository.save(
                    NotificationLog.builder()
                            .notificationId(notification.getId())
                            .status("ATTEMPT")
                            .kafkaOffset(offset)
                            .partitionId(partition)
                            .build()
            );

            emailService.send(event);

            notification.setStatus("SENT");
            notification.setSentAt(Instant.now());

            notificationRepo.save(notification);
            // ================= Log Success =================
            notificationLogRepository.save(
                    NotificationLog.builder()
                            .notificationId(notification.getId())
                            .status("SUCCESS")
                            .kafkaOffset(offset)
                            .partitionId(partition)
                            .build()
            );

            log.info("Notification sent tx={}", event.getTransactionId());

        } catch (Exception e) {

            log.error("Notification failed", e);

            if (notification != null) {

                // Update notification
                notification.setStatus("FAILED");
                notification.setRetryCount(
                        notification.getRetryCount() + 1
                );
                notification.setLastError(e.getMessage());

                notificationRepo.save(notification);

                // ================= Log Failure =================
                notificationLogRepository.save(
                        NotificationLog.builder()
                                .notificationId(notification.getId())
                                .status("FAIL")
                                .errorMessage(e.getMessage())
                                .kafkaOffset(offset)
                                .partitionId(partition)
                                .build()
                );
            }
        }
    }
}