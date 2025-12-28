package com.subhanmishra.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class FirstRetryOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FirstRetryOrderEventConsumer.class);

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public FirstRetryOrderEventConsumer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "order.events.retry-100ms",
            groupId = "course-service-retry-100ms"
    )
    public void consume(OrderEvent event, Acknowledgment ack) {
        try {
            // Backoff
            Thread.sleep(100);
            activateCourse(event.courseId(), event.userId());
            ack.acknowledge();
        } catch (TransientDownstreamException ex) {
            log.warn("Still failing after 100ms, routing to retry-200ms for orderId={}", event.id(), ex);
            kafkaTemplate.send("order.events.retry-200ms",
                    String.valueOf(event.id()), event);
            ack.acknowledge();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("Unexpected failure in 100ms retry, routing to DLQ for orderId={}", event.id(), ex);
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
