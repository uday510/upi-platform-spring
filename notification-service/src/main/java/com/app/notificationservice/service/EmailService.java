package com.app.notificationservice.service;

import com.app.notificationservice.dto.TransferCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void send(TransferCompletedEvent event) {

        // Replace later with SMTP / SES

        log.info("""
            Sending email:
            User: {}
            Amount: {}
            TxId: {}
            """,
                event.getUserId(),
                event.getAmount(),
                event.getTransactionId()
        );

    }
}
