CREATE KEYSPACE cas WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE cas.ticket_cleaner (
    expiry_type text,
    date_bucket bigint,
    id text,
    PRIMARY KEY ((expiry_type, date_bucket), id)
);

CREATE TABLE cas.ticketgrantingticket (
    id text PRIMARY KEY,
    ticket blob
) WITH default_time_to_live = 5184000;

CREATE TABLE cas.serviceticket (
    id text PRIMARY KEY,
    ticket blob
) WITH default_time_to_live = 60;

CREATE TABLE cas.ticket_cleaner_lastrun (
    id text PRIMARY KEY,
    last_run bigint
);
