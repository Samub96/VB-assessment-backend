package com.assesment.backpayments.infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import org.h2.api.Trigger;

/**
 * Trigger de H2 que registra cambios de estado de órdenes.
 */
public class OrderStatusAuditTrigger implements Trigger {

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) {
        // Sin estado adicional.
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        if (oldRow == null || newRow == null || Objects.equals(oldRow[1], newRow[1])) {
            return;
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "insert into order_status_log (id, order_id, from_status, to_status, changed_at, changed_by) values (?, ?, ?, ?, current_timestamp, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, newRow[0]);
            statement.setString(3, String.valueOf(oldRow[1]));
            statement.setString(4, String.valueOf(newRow[1]));
            statement.setObject(5, "APPROVED".equals(newRow[1]) ? newRow[9] : newRow[10]);
            statement.executeUpdate();
        }
    }

    @Override
    public void close() {
        // Sin recursos persistentes.
    }

    @Override
    public void remove() {
        // Sin recursos persistentes.
    }
}
