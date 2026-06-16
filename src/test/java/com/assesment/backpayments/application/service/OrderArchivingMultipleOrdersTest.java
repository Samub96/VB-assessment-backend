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
class OrderArchivingMultipleOrdersTest {

    @Autowired
    private OrderArchivingService archivingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void archiveMultipleRejectedOrders() {
        UUID userId = UUID.randomUUID();
        UUID order1 = UUID.randomUUID();
        UUID order2 = UUID.randomUUID();
        UUID log1 = UUID.randomUUID();
        UUID log2 = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO users (id, email, password_hash) VALUES (?,?,?)",
                userId.toString(), "u@example.com", "h");

        jdbcTemplate.update("INSERT INTO orders (id, status, amount, currency, created_at, created_by) VALUES (?,?,?,?,?,?)",
                order1.toString(), "REJECTED", new BigDecimal("10.00"), "USD", Timestamp.from(Instant.now()), userId.toString());
        jdbcTemplate.update("INSERT INTO orders (id, status, amount, currency, created_at, created_by) VALUES (?,?,?,?,?,?)",
                order2.toString(), "REJECTED", new BigDecimal("20.00"), "USD", Timestamp.from(Instant.now()), userId.toString());

        jdbcTemplate.update("INSERT INTO order_status_log (id, order_id, from_status, to_status, changed_at, changed_by) VALUES (?,?,?,?,?,?)",
                log1.toString(), order1.toString(), "PENDING", "REJECTED", Timestamp.from(Instant.now()), userId.toString());
        jdbcTemplate.update("INSERT INTO order_status_log (id, order_id, from_status, to_status, changed_at, changed_by) VALUES (?,?,?,?,?,?)",
                log2.toString(), order2.toString(), "PENDING", "REJECTED", Timestamp.from(Instant.now()), userId.toString());

        archivingService.archiveRejectedOrders();

        Integer a1 = jdbcTemplate.queryForObject("SELECT count(*) FROM archived_orders WHERE id = ?", Integer.class, order1.toString());
        Integer a2 = jdbcTemplate.queryForObject("SELECT count(*) FROM archived_orders WHERE id = ?", Integer.class, order2.toString());
        assertEquals(1, a1.intValue());
        assertEquals(1, a2.intValue());

        Integer oCount = jdbcTemplate.queryForObject("SELECT count(*) FROM orders WHERE status = 'REJECTED'", Integer.class);
        assertEquals(0, oCount.intValue());
    }
}
