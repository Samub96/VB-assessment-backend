package com.assesment.backpayments.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OrderArchivingIntegrationTest {

    @Autowired
    private OrderArchivingService archivingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void archiveRejectedOrders_movesRowsToArchivedTables() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        // insert minimal user
        jdbcTemplate.update("INSERT INTO users (id, email, password_hash) VALUES (?,?,?)",
                userId.toString(), "test@example.com", "hash");

        // insert rejected order
        jdbcTemplate.update("INSERT INTO orders (id, status, amount, currency, created_at, created_by) VALUES (?,?,?,?,?,?)",
                orderId.toString(), "REJECTED", new BigDecimal("123.45"), "USD", Timestamp.from(Instant.now()), userId.toString());

        // insert status log
        jdbcTemplate.update("INSERT INTO order_status_log (id, order_id, from_status, to_status, changed_at, changed_by) VALUES (?,?,?,?,?,?)",
                logId.toString(), orderId.toString(), "PENDING", "REJECTED", Timestamp.from(Instant.now()), userId.toString());

        // invoke archiving
        archivingService.archiveRejectedOrders();

        Integer archivedOrderCount = jdbcTemplate.queryForObject("SELECT count(*) FROM archived_orders WHERE id = ?", Integer.class, orderId.toString());
        assertEquals(1, archivedOrderCount.intValue());

        Integer originalOrderCount = jdbcTemplate.queryForObject("SELECT count(*) FROM orders WHERE id = ?", Integer.class, orderId.toString());
        assertEquals(0, originalOrderCount.intValue());

        Integer archivedLogCount = jdbcTemplate.queryForObject("SELECT count(*) FROM archived_order_status_log WHERE id = ?", Integer.class, logId.toString());
        assertEquals(1, archivedLogCount.intValue());
    }
}
