package com.subhanmishra.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrderService {

    public static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String ORDER_EVENTS_TOPIC = "order_events";

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository,
                        OrderOutboxRepository orderOutboxRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderOutboxRepository = orderOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Order createOrder(OrderRequest request) throws JsonProcessingException {
        log.info("Received order for course {} for user {}", request.courseId(), request.userId());
        // 1. Save Order to database
        Order order = new Order(
                null,
                request.userId(),
                request.courseId(),
                request.amount(),
                "CREATED");

        order = orderRepository.save(order);

        // 2. Save Event to outbox table in same transaction
        OrderEvent event = new OrderEvent(
                order.id(),
                order.userId(),
                order.courseId(),
                order.amount(),
                order.status()
        );

        OrderOutbox orderOutbox = new OrderOutbox(null, "ORDER", order.id(), "ORDER_CREATED", objectMapper.writeValueAsString(event), "NEW", Instant.now(), null);
        OrderOutbox savedOrderOutbox = orderOutboxRepository.save(orderOutbox);
        log.info("Saved orderEvent to outbox table with id {} at timestamp {}", savedOrderOutbox.id(), savedOrderOutbox.createdAt());

        return order;
    }
}
