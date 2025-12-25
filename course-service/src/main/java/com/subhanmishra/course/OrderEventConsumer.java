package com.subhanmishra.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(topics = "order_events", groupId = "course-service-group")
    public void consume(OrderEvent event) {
        log.info("Received order event: {}", event);
        // Activate course logic here
        activateCourse(event.courseId(), event.userId());
    }

    private void activateCourse(Long courseId, Long userId) {
        log.info("Activating course {} for user {}", courseId, userId);
        // Implementation will be added
    }
}
