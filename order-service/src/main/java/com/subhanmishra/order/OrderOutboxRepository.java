package com.subhanmishra.order;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface OrderOutboxRepository extends ListCrudRepository<OrderOutbox, Long> {

    List<OrderOutbox> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
