package com.subhanmishra.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final IdempotencyService idempotencyService;

    public OrderEventConsumer(KafkaTemplate<String, OrderEvent> kafkaTemplate, IdempotencyService idempotencyService) {
        this.kafkaTemplate = kafkaTemplate;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(topics = "order_events", groupId = "course-service-group")
    public void consume(OrderEvent event, Acknowledgment ack) {
        String idempotencyKey = "idempotency:course-service:" + event.eventId();
        // If we've seen this eventId before, skip side effects
        if (idempotencyService.isDone(idempotencyKey)) {
            log.info("Skipping duplicate eventId={} for orderId={} for courseId={} userId={}", event.eventId(), event.id(), event.courseId(), event.userId());
            ack.acknowledge();
            return;
        }
        try {
            activateCourse(event.courseId(), event.userId());
            idempotencyService.markDone(idempotencyKey); // mark as processed
            ack.acknowledge();
        } catch (TransientDownstreamException ex) {
            log.warn("Transient failure for orderId={}, routing to retry-100ms", event.id());
            kafkaTemplate.send("order.events.retry-100ms",
                    String.valueOf(event.id()), event);
            ack.acknowledge(); // We handled it by moving it to retry
        } catch (Exception ex) {
            log.error("Unexpected failure for orderId={}, routing to DLQ", event.id(), ex);
            kafkaTemplate.send("order.events.dlq",
                    String.valueOf(event.id()), event);
            ack.acknowledge();
        }

    }

    private void activateCourse(Long courseId, Long userId) {
        if (Math.random() < 0.3) {
            log.warn("Downstream system is slow/unavailable for course {}", courseId);
            throw new TransientDownstreamException("Downstream timeout");
        }
        // Simulate some processing time
//        try {
//            Thread.sleep(500); // 0.5s
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
        log.info("Finished activation for course {} and user {}", courseId, userId);
    }
}
