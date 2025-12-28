package com.subhanmishra.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class OutboxCdcListener {

    private static final Logger log = LoggerFactory.getLogger(OutboxCdcListener.class);

    private static final String CDC_TOPIC = "dbz.edtech_orders.outbox_events";
    private static final String ORDER_EVENTS_TOPIC = "order_events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxCdcListener(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = CDC_TOPIC, groupId = "event-transformer-group")
    public void handleCdc(String message,
                          @Header(
                                  KafkaHeaders.OFFSET) long offset,
                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) throws JsonProcessingException {

        log.info("processing message from partition={}, offset={}", partition, offset);
        // ... use 'message' as before
        JsonNode root = objectMapper.readTree(message);

        // Debezium JSON: { "schema": {...}, "payload": { "before": ..., "after": {...}, ... } }
        JsonNode payloadNode = root.get("payload");
        if (payloadNode == null || payloadNode.isNull()) {
            return;
        }

        JsonNode after = payloadNode.get("after");
        if (after == null || after.isNull()) {
            // ignore deletes/updates for now
            log.info("after is null for partition={}, offset={}", partition, offset);
            return;
        }

        String aggregateType = after.get("aggregate_type").asText();
        if (!"ORDER".equals(aggregateType)) {
            // in case you add other aggregates later
            log.info("Aggregate type is different from 'ORDER' partition={}, offset={}", partition, offset);
            return;
        }

        String eventType = after.get("type").asText();
        String payloadJson = after.get("payload").asText(); // JSON-as-string "payload": "{\"id\":2,\"amount\":100.0,\"status\":\"CREATED\",\"userId\":3,\"courseId\":15}"

        JsonNode payload = objectMapper.readTree(payloadJson);

        // Build clean business event
        ObjectNode cleanEvent = objectMapper.createObjectNode();
        cleanEvent.put("eventType", eventType); // optional, consumers can ignore
        cleanEvent.setAll((ObjectNode) payload); // orderId, userId, courseId, status

        String key = payload.get("id").asText();
        String value = objectMapper.writeValueAsString(cleanEvent);

        kafkaTemplate.send(ORDER_EVENTS_TOPIC, key, value);
        log.info("Transformed CDC outbox event into order_events: key={}, value={}", key, value);
    }
}
