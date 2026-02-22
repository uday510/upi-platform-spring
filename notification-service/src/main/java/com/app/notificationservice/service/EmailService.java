package com.app.notificationservice.service;

import com.app.notificationservice.dto.TransferCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void send(String email, TransferCompletedEvent event) {

        // TODO: Replace with SMTP / SES / SendGrid later

        String message = String.format("""
                -------------------------
                To      : %s
                Amount  : %s
                TxId    : %s
                Status  : SUCCESS
                -------------------------
                """,
                email,
                event.getAmount(),
                event.getTransactionId()
        );

        log.info(message);
    }
}