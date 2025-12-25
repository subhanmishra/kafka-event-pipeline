package com.subhanmishra.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(topics = "order_events", groupId = "notification-service-group")
    public void consume(OrderEvent event) {
        log.info("Sending notification for order: {}", event.id());
        sendEmail(event.userId(), event);
    }

    private void sendEmail(Long userId, OrderEvent event) {
        log.info("Email sent to user {} for order {}", userId, event.id());
    }
}
