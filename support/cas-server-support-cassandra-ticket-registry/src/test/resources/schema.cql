CREATE KEYSPACE IF NOT EXISTS cas2 WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE IF NOT EXISTS cas2.ticket_cleaner (
    expiry_type text,
    date_bucket bigint,
    id text,
    PRIMARY KEY ((expiry_type, date_bucket), id)
);

CREATE TABLE IF NOT EXISTS cas2.ticketgrantingticket (
    id text PRIMARY KEY,
    ticket text
) WITH default_time_to_live = 5184000;

CREATE TABLE IF NOT EXISTS cas2.serviceticket (
    id text PRIMARY KEY,
    ticket text
) WITH default_time_to_live = 60;

CREATE TABLE IF NOT EXISTS cas2.ticket_cleaner_lastrun (
    id text PRIMARY KEY,
    last_run bigint
);
