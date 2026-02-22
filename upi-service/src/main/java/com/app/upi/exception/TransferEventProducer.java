package com.app.upi.exception;


import com.app.upi.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransferEventProducer {

    private static final String TOPIC =
            "upi.transfer.completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;


    public void publish(TransferCompletedEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            log.info("Publishing transfer event: txId={}",
                    event.getTransactionId());

            kafkaTemplate.send(
                    TOPIC,
                    event.getTransactionId().toString(),
                    payload
            );

        } catch (Exception e) {
            log.error("Failed to publish event {}", event.getTransactionId(), e);
            throw new RuntimeException("Kafka publish failed", e);
        }

    }
}


