package com.assesment.backpayments.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Procedimiento auxiliar para H2 que archiva órdenes rechazadas.
 */
public final class RejectedOrderArchiveProcedure {

    private RejectedOrderArchiveProcedure() {
    }

    public static void archiveRejectedOrders(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    insert into archived_orders (
                        id,
                        status,
                        amount,
                        currency,
                        description,
                        invoice_key,
                        created_at,
                        updated_at,
                        created_by,
                        approved_by,
                        rejected_by,
                        approved_at,
                        rejected_at,
                        archived_at
                    )
                    select
                        o.id,
                        o.status,
                        o.amount,
                        o.currency,
                        o.description,
                        o.invoice_key,
                        o.created_at,
                        o.updated_at,
                        o.created_by,
                        o.approved_by,
                        o.rejected_by,
                        o.approved_at,
                        o.rejected_at,
                        current_timestamp
                    from orders o
                    where o.status = 'REJECTED'
                      and not exists (
                          select 1
                          from archived_orders ao
                          where ao.id = o.id
                      )
                    """);
            statement.executeUpdate("""
                    insert into archived_order_status_log (
                        id,
                        order_id,
                        from_status,
                        to_status,
                        changed_at,
                        changed_by,
                        archived_at
                    )
                    select
                        l.id,
                        l.order_id,
                        l.from_status,
                        l.to_status,
                        l.changed_at,
                        l.changed_by,
                        current_timestamp
                    from order_status_log l
                    join orders o on o.id = l.order_id
                    where o.status = 'REJECTED'
                      and not exists (
                          select 1
                          from archived_order_status_log aol
                          where aol.id = l.id
                      )
                    """);
            statement.executeUpdate("delete from orders where status = 'REJECTED'");
        }
    }
}
