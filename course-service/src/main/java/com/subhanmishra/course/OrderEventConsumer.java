package com.subhanmishra.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final CourseActivationRepository courseActivationRepository;

    public OrderEventConsumer(CourseActivationRepository courseActivationRepository) {
        this.courseActivationRepository = courseActivationRepository;
    }

    @KafkaListener(topics = "order_events", groupId = "course-service-group")
    public void consume(OrderEvent event) {
        log.info("Received order event: {}", event);
        // Activate course logic here
        activateCourse(event.courseId(), event.userId());

        courseActivationRepository.save(
                new CourseActivation(null, event.id(), event.userId(), event.courseId(), "ACTIVATED", Instant.now()));
    }

    private void activateCourse(Long courseId, Long userId) {
        log.info("Activating course {} for user {}", courseId, userId);
        // Simulate work
//        try {
//            Thread.sleep(5000); // 5 seconds
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
        log.info("Finished activation for course {} and user {}", courseId, userId);
    }
}
