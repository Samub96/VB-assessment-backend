package com.assesment.backpayments.application.service;

import com.assesment.backpayments.application.dto.ArchivedOrderSummaryResponse;
import com.assesment.backpayments.domain.model.OrderStatus;
import com.assesment.backpayments.infrastructure.storage.OrderRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ejecuta el archivado de órdenes rechazadas mediante el procedimiento de base de datos.
 */
@Service
public class OrderArchivingService {

    private final OrderRepository orderRepository;

    public OrderArchivingService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void archiveRejectedOrders() {
        orderRepository.archiveRejectedOrders();
    }

    @Transactional(readOnly = true)
    public List<ArchivedOrderSummaryResponse> listArchivedOrders() {
        return orderRepository.listArchivedOrders().stream()
                .map(view -> new ArchivedOrderSummaryResponse(
                        UUID.fromString(view.getId()),
                        OrderStatus.valueOf(view.getStatus()),
                        view.getAmount(),
                        view.getCurrency(),
                        toInstant(view.getCreatedAt()),
                        toInstant(view.getArchivedAt())
                ))
                .toList();
    }

    private Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        throw new IllegalStateException("Tipo temporal no soportado para archivadas: " + value);
    }
}
