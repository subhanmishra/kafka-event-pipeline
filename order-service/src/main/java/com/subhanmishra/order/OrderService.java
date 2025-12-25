package com.subhanmishra.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    public static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String ORDER_EVENTS_TOPIC = "order_events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        KafkaTemplate<Object, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("Received order for course {} for user {}", request.courseId(), request.userId());
        // 1. Save to database
        Order order = new Order(
                null,
                request.userId(),
                request.courseId(),
                request.amount(),
                "CREATED");

        order = orderRepository.save(order);

        // 2. Publish to Kafka (dual-write)
        OrderEvent event = new OrderEvent(
                order.id(),
                order.userId(),
                order.courseId(),
                order.amount(),
                order.status()
        );
        log.info("Saved order for course {} for user {}", request.courseId(), request.userId());
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, String.valueOf(order.id()), event);

        return order;
    }
}
