package com.subhanmishra.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("orders")
public record Order(@Id Long id,
                    Long userId,
                    Long courseId,
                    BigDecimal amount,
                    String status) {
}
