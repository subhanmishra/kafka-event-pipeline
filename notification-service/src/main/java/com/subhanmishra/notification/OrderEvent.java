package com.subhanmishra.notification;

import java.math.BigDecimal;

public record OrderEvent(Long id, Long userId, Long courseId, BigDecimal amount, String status) {
}
