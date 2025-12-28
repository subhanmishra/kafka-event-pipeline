package com.subhanmishra.course;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("course_activations")
public record CourseActivation(@Id
                               Long id,
                               Long orderId,
                               Long userId,
                               Long courseId,
                               String status,      // e.g. ACTIVATED
                               Instant processedAt) {
}
