package com.subhanmishra.order;

import java.math.BigDecimal;

public record OrderRequest(Long userId, Long courseId,
                           BigDecimal amount) {
}
