package com.assesment.backpayments.infrastructure.storage;

import com.assesment.backpayments.domain.model.Order;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repositorio de órdenes de pago.
 */
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "CALL archive_rejected_orders()", nativeQuery = true)
    void archiveRejectedOrders();
}
