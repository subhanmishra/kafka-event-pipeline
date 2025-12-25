package com.subhanmishra.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("outbox_events")
public record OrderOutbox(@Id Long id,
                          String aggregateType,
                          Long aggregateId,
                          String type,
                          String payload,
                          String status, // e.g. NEW, SENT
                          Instant createdAt,
                          Instant sentAt) {
}
