package com.app.upi.exception;


import com.app.upi.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferEventProducer {

    private static final String TOPIC =
            "upi.transfer.completed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(TransferCompletedEvent event) {

        log.info("Publishing transfer event: txId={}",
                event.getTransactionId());

        kafkaTemplate.send(
                TOPIC,
                event.getTransactionId().toString(),
                event
        );
    }

}


