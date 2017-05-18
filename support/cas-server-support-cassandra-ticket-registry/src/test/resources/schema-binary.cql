CREATE KEYSPACE IF NOT EXISTS cas_binary WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE IF NOT EXISTS cas_binary.ticket_cleaner (
    expiry_type text,
    date_bucket bigint,
    id text,
    PRIMARY KEY ((expiry_type, date_bucket), id)
);

CREATE TABLE IF NOT EXISTS cas_binary.ticketgrantingticket (
    id text PRIMARY KEY,
    ticket blob
) WITH default_time_to_live = 5184000;

CREATE TABLE IF NOT EXISTS cas_binary.serviceticket (
    id text PRIMARY KEY,
    ticket blob
) WITH default_time_to_live = 60;

CREATE TABLE IF NOT EXISTS cas_binary.ticket_cleaner_lastrun (
    id text PRIMARY KEY,
    last_run bigint
);
