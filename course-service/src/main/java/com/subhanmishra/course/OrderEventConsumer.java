package com.subhanmishra.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final CourseActivationRepository courseActivationRepository;
    private final ExecutorService orderProcessingExecutor;

    public OrderEventConsumer(CourseActivationRepository courseActivationRepository, ExecutorService orderProcessingExecutor) {
        this.courseActivationRepository = courseActivationRepository;
        this.orderProcessingExecutor = orderProcessingExecutor;
    }

    @KafkaListener(topics = "order_events", groupId = "course-service-group")
    public void consume(OrderEvent event) {
        log.info("Received order event (handing off to async): {}", event);
        // Hand off work to a pool; listener returns immediately
        orderProcessingExecutor.submit(() -> {
            try {
                log.info("Async processing START for orderId={}", event.id());
                activateCourse(event.courseId(), event.userId());
                courseActivationRepository.save(
                        new CourseActivation(null, event.id(), event.userId(), event.courseId(), "ACTIVATED", Instant.now()));
                log.info("Async processing DONE for orderId={}", event.id());
            } catch (Exception ex) {
                log.error("Async processing FAILED for orderId={}", event.id(), ex);
            }
        });

    }

    private void activateCourse(Long courseId, Long userId) {
        log.info("Activating course {} for user {}", courseId, userId);
        // Simulate work
        try {
            Thread.sleep(5000); // 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Finished activation for course {} and user {}", courseId, userId);
    }
}
