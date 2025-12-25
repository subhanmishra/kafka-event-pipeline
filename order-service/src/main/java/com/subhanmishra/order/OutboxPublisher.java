package com.subhanmishra.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final String ORDER_EVENTS_TOPIC = "order_events";


    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OrderOutboxRepository orderOutboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderOutboxRepository = orderOutboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000) // every second
    public void publishNewEvents() {
        List<OrderOutbox> events = orderOutboxRepository
                .findTop100ByStatusOrderByCreatedAtAsc("NEW");
        for (OrderOutbox event : events) {
            try {
                kafkaTemplate.send(ORDER_EVENTS_TOPIC,
                        String.valueOf(event.aggregateId()),
                        event.payload());
                OrderOutbox updatedEvent = new OrderOutbox(event.id(), event.aggregateType(), event.aggregateId(), event.type(), event.payload(), "SENT", event.createdAt(), Instant.now());
                OrderOutbox savedEvent = orderOutboxRepository.save(updatedEvent);
                log.info("Published outbox event id={}", savedEvent.id());
            } catch (Exception ex) {
                log.error("Failed to publish outbox event id={}", event.id(), ex);
                // Leave status as NEW so we can retry on next run
            }
        }
    }
}
