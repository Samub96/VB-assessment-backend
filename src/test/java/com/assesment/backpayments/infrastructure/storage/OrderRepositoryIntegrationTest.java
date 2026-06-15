package com.assesment.backpayments.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void repositoryCallExecutesArchiveProcedure() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO users (id, email, password_hash) VALUES (?,?,?)",
                userId.toString(), "r@example.com", "h");

        jdbcTemplate.update("INSERT INTO orders (id, status, amount, currency, created_at, created_by) VALUES (?,?,?,?,?,?)",
                orderId.toString(), "REJECTED", new BigDecimal("5.00"), "USD", Timestamp.from(Instant.now()), userId.toString());

        jdbcTemplate.update("INSERT INTO order_status_log (id, order_id, from_status, to_status, changed_at, changed_by) VALUES (?,?,?,?,?,?)",
                logId.toString(), orderId.toString(), "PENDING", "REJECTED", Timestamp.from(Instant.now()), userId.toString());

        orderRepository.archiveRejectedOrders();

        Integer archived = jdbcTemplate.queryForObject("SELECT count(*) FROM archived_orders WHERE id = ?", Integer.class, orderId.toString());
        assertEquals(1, archived.intValue());

        List<OrderRepository.ArchivedOrderView> archivedList = orderRepository.listArchivedOrders();
        assertTrue(archivedList.stream().anyMatch(item -> orderId.toString().equals(item.getId())));
    }
}
