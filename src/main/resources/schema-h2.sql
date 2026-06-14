create table roles (
    name varchar(32) not null primary key
)@@

create table users (
    id uuid not null primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null
)@@

create table user_roles (
    user_id uuid not null,
    role_name varchar(32) not null,
    primary key (user_id, role_name),
    constraint fk_user_roles_user foreign key (user_id) references users (id),
    constraint fk_user_roles_role foreign key (role_name) references roles (name)
)@@

create table orders (
    id uuid not null primary key,
    status varchar(32) not null,
    amount numeric(12, 2) not null,
    currency varchar(8) not null,
    description varchar(255),
    invoice_key varchar(512),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    created_by uuid not null,
    approved_by uuid,
    rejected_by uuid,
    approved_at timestamp with time zone,
    rejected_at timestamp with time zone,
    constraint fk_orders_created_by foreign key (created_by) references users (id),
    constraint fk_orders_approved_by foreign key (approved_by) references users (id),
    constraint fk_orders_rejected_by foreign key (rejected_by) references users (id),
    constraint chk_orders_status check (status in ('PENDING', 'APPROVED', 'REJECTED'))
)@@

create index idx_orders_status_created_at on orders (status, created_at desc)@@

create table order_status_log (
    id uuid not null primary key,
    order_id uuid not null,
    from_status varchar(32) not null,
    to_status varchar(32) not null,
    changed_at timestamp with time zone not null,
    changed_by uuid,
    constraint fk_order_status_log_order foreign key (order_id) references orders (id) on delete cascade,
    constraint fk_order_status_log_changed_by foreign key (changed_by) references users (id),
    constraint chk_order_status_log_status check (from_status in ('PENDING', 'APPROVED', 'REJECTED') and to_status in ('PENDING', 'APPROVED', 'REJECTED'))
)@@

create index idx_order_status_log_order_id on order_status_log (order_id)@@

create table integration_response_log (
    id uuid not null primary key,
    order_id uuid not null,
    provider varchar(120) not null,
    success boolean not null,
    status_code integer not null,
    request_body varchar(4000) not null,
    response_body varchar(8000),
    error_message varchar(2000),
    executed_at timestamp with time zone not null,
    constraint fk_integration_response_log_order foreign key (order_id) references orders (id) on delete cascade
)@@

create index idx_integration_response_log_order_id on integration_response_log (order_id)@@

create table archived_orders (
    id uuid not null primary key,
    status varchar(32) not null,
    amount numeric(12, 2) not null,
    currency varchar(8) not null,
    description varchar(255),
    invoice_key varchar(512),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    created_by uuid not null,
    approved_by uuid,
    rejected_by uuid,
    approved_at timestamp with time zone,
    rejected_at timestamp with time zone,
    archived_at timestamp with time zone not null,
    constraint fk_archived_orders_created_by foreign key (created_by) references users (id),
    constraint fk_archived_orders_approved_by foreign key (approved_by) references users (id),
    constraint fk_archived_orders_rejected_by foreign key (rejected_by) references users (id),
    constraint chk_archived_orders_status check (status in ('PENDING', 'APPROVED', 'REJECTED'))
)@@

create table archived_order_status_log (
    id uuid not null primary key,
    order_id uuid not null,
    from_status varchar(32) not null,
    to_status varchar(32) not null,
    changed_at timestamp with time zone not null,
    changed_by uuid,
    archived_at timestamp with time zone not null,
    constraint fk_archived_order_status_log_order foreign key (order_id) references archived_orders (id) on delete cascade,
    constraint fk_archived_order_status_log_changed_by foreign key (changed_by) references users (id),
    constraint chk_archived_order_status_log_status check (from_status in ('PENDING', 'APPROVED', 'REJECTED') and to_status in ('PENDING', 'APPROVED', 'REJECTED'))
)@@

create index idx_archived_order_status_log_order_id on archived_order_status_log (order_id)@@

create trigger trg_orders_status_audit
after update on orders
for each row
call "com.assesment.backpayments.infrastructure.database.OrderStatusAuditTrigger"@@

create alias archive_rejected_orders for "com.assesment.backpayments.infrastructure.database.RejectedOrderArchiveProcedure.archiveRejectedOrders"@@
