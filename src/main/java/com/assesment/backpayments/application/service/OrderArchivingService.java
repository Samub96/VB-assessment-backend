package com.assesment.backpayments.application.service;

import com.assesment.backpayments.infrastructure.storage.OrderRepository;
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
}
