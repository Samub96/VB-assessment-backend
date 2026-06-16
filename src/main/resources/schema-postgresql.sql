create table if not exists roles (
    name varchar(32) not null primary key
)@@

create table if not exists users (
    id uuid not null primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null
)@@

create table if not exists user_roles (
    user_id uuid not null,
    role_name varchar(32) not null,
    primary key (user_id, role_name),
    constraint fk_user_roles_user foreign key (user_id) references users (id),
    constraint fk_user_roles_role foreign key (role_name) references roles (name)
)@@

create table if not exists orders (
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

create index if not exists idx_orders_status_created_at on orders (status, created_at desc)@@

create table if not exists order_status_log (
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

create index if not exists idx_order_status_log_order_id on order_status_log (order_id)@@

create table if not exists integration_response_log (
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

create index if not exists idx_integration_response_log_order_id on integration_response_log (order_id)@@

create table if not exists archived_orders (
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

create table if not exists archived_order_status_log (
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

create index if not exists idx_archived_order_status_log_order_id on archived_order_status_log (order_id)@@

create or replace function generate_uuid()
returns uuid
language sql
as $$
    select (
        substr(md5(random()::text || clock_timestamp()::text), 1, 8) || '-' ||
        substr(md5(random()::text || clock_timestamp()::text), 9, 4) || '-' ||
        substr(md5(random()::text || clock_timestamp()::text), 13, 4) || '-' ||
        substr(md5(random()::text || clock_timestamp()::text), 17, 4) || '-' ||
        substr(md5(random()::text || clock_timestamp()::text), 21, 12)
    )::uuid;
$$@@

create or replace function trg_orders_status_audit()
returns trigger
language plpgsql
as $$
begin
    if new.status is distinct from old.status then
        insert into order_status_log (id, order_id, from_status, to_status, changed_at, changed_by)
        values (
            generate_uuid(),
            new.id,
            old.status,
            new.status,
            current_timestamp,
            case
                when new.status = 'APPROVED' then new.approved_by
                when new.status = 'REJECTED' then new.rejected_by
                else null
            end
        );
    end if;

    return new;
end;
$$@@

drop trigger if exists trg_orders_status_audit on orders@@
create trigger trg_orders_status_audit
after update on orders
for each row
execute function trg_orders_status_audit()@@

create or replace procedure archive_rejected_orders()
language plpgsql
as $$
begin
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
      );

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
      );

    delete from orders
    where status = 'REJECTED';
end;
$$@@
