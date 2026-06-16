package com.assesment.backpayments.infrastructure.storage;

import com.assesment.backpayments.domain.model.Order;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio de órdenes de pago.
 */
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "CALL archive_rejected_orders()", nativeQuery = true)
    void archiveRejectedOrders();

    @Query(value = """
            SELECT
                CAST(id AS VARCHAR) AS id,
                status AS status,
                amount AS amount,
                currency AS currency,
                created_at AS createdAt,
                archived_at AS archivedAt
            FROM archived_orders
            ORDER BY archived_at DESC
            """, nativeQuery = true)
    List<ArchivedOrderView> listArchivedOrders();

    interface ArchivedOrderView {
        String getId();

        String getStatus();

        BigDecimal getAmount();

        String getCurrency();

        Object getCreatedAt();

        Object getArchivedAt();
    }
}
