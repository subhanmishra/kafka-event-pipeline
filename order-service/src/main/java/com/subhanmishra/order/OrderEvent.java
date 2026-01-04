package com.subhanmishra.order;


import java.math.BigDecimal;


record OrderEvent(Long id, Long userId, Long courseId, BigDecimal amount,String eventType, String eventId, String status) {
}
