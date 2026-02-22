package com.app.upi.service;

import com.app.upi.entity.OutboxEvent;
import com.app.upi.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository repo;
    private final KafkaTemplate<String, String> kafka;

    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {

        List<OutboxEvent> events =
                repo.findForPublishing(PageRequest.of(0, 100));

        if (events.isEmpty()) {
            return;
        }

        log.info("Publishing {} outbox events", events.size());

        for (OutboxEvent event : events) {

            try {

                // BLOCK until Kafka confirms send
                kafka.send(
                        "transfer.completed",
                        event.getAggregateId().toString(),
                        event.getPayload()
                ).get();

                event.markSent();

                log.info("Published event {}", event.getId());

            } catch (Exception ex) {

                log.error("Publish failed for {}", event.getId(), ex);

                event.incrementRetry(ex.getMessage(), MAX_RETRIES);

                if (event.isFailed()) {
                    sendToDLQ(event);
                }
            }
        }

        repo.saveAll(events);
    }

    private void sendToDLQ(OutboxEvent event) {

        try {

            kafka.send(
                    "transfer.completed.dlq",
                    event.getAggregateId().toString(),
                    event.getPayload()
            ).get();

            log.warn("Moved to DLQ: {}", event.getId());

        } catch (Exception e) {
            log.error("DLQ publish failed: {}", event.getId(), e);
        }
    }
}