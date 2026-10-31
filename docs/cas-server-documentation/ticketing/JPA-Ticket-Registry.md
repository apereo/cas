---
layout: default
title: CAS - JPA Ticket Registry
category: Ticketing
---

{% include variables.html %}

# JPA Ticket Registry

The JPA Ticket Registry allows CAS to store tickets in a relational database back-end such as MySQL.

<div class="alert alert-info">:information_source: <strong>Usage Warning!</strong><p>Using a relational database as
the back-end persistence choice for ticket registry state management can be complicated. 
Unless you are already outfitted with clustered database technology and the resources to manage it,
the complexity is usually not worth the trouble.</p></div>

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-ticket-registry" %}

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.jpa" %}

<div class="alert alert-info">:information_source: <strong>Start With A Clean Table</strong><p>New deployments of CAS,
and upgrades to a new CAS version, should always start with a clean ticket registry table: drop the existing table and let
CAS create it. Schema updates add missing tables, columns and indexes, but they do not change the type or length of an
existing column, so a table created by an earlier version can keep a schema that the current version does not expect.
Tickets are short-lived, and dropping the table only ends the single sign-on sessions it holds.</p></div>

## Expiration

Each ticket row stores the time the ticket expires unless it is used again: the earlier of its maximum and its idle
expiration time, or the time it was saved for a ticket that was already expired, such as one marked expired. The row is
rewritten each time the ticket is updated, so the idle expiration follows its last use. The column is indexed, and
the [ticket registry cleaner](Ticket-Registry-Cleaner.html) reads only the rows whose time has passed, apart from a
full scan at startup and hourly. Rows written by earlier versions hold the maximum expiration time; they are caught
by those full scans until their tickets are next updated. Schema updates add the index to an existing table; with
schema generation turned off, create it by hand:

```sql
CREATE INDEX idx_ticket_expiration ON cas_tickets (expiration_time);
```

## MySQL

To scan large tables without reading every row into memory, CAS sets `useCursorFetch=true` for MySQL, together with
`cachePrepStmts=true`, `prepStmtCacheSize=250` and `prepStmtCacheSqlLimit=2048`, unless the
`cas.ticket.registry.jpa.properties`{: .cas-setting} settings or the JDBC URL already set them. Connector/J uses a
server-side cursor only for statements with a fetch size, so CAS applies the configured
`cas.ticket.registry.jpa.fetch-size`{: .cas-setting} only to queries that stream tickets, such as the cleaner's scan;
single-ticket lookups read their row directly. With `useCursorFetch`, Connector/J prepares every statement on the
server, and the prepared statement cache keeps that to one round trip per lookup.

MySQL limits an index key to 3072 bytes, which with the `utf8mb4` character set is 768 characters. On MySQL the
ticket table therefore declares `type` as `VARCHAR(255)`, `principal_id` as `VARCHAR(512)` and `parent_id` as
`VARCHAR(768)`, so that its indexes on the principal, the parent ticket and the ticket type with the principal can be
created; the `service` column keeps its length and is not indexed on MySQL. Tables created by earlier versions
declared longer columns, and MySQL refused every one of those indexes, so lookups by principal or parent ticket scanned
the whole table. Schema updates do not shorten existing columns: start with a clean table, as recommended above, or
apply the change to the existing table by hand:

```sql
ALTER TABLE cas_tickets
    MODIFY type VARCHAR(255) NOT NULL,
    MODIFY principal_id VARCHAR(512),
    MODIFY parent_id VARCHAR(768);
CREATE INDEX idx_ticket_principal ON cas_tickets (principal_id);
CREATE INDEX idx_ticket_parent ON cas_tickets (parent_id);
CREATE INDEX idx_type_principal ON cas_tickets (type, principal_id);
```

The ticket table no longer declares an index on `type` alone, since the index on the ticket type and the principal
serves the same queries. Schema updates do not drop indexes either; an existing `idx_ticket_type` can be removed with
`DROP INDEX idx_ticket_type ON cas_tickets` (or `DROP INDEX idx_ticket_type` where indexes are not scoped to a table).

## PostgreSQL

On PostgreSQL, ticket attributes are stored as `jsonb`, so that finding single sign-on sessions by attribute, as
delegated OpenID Connect and SAML2 logout do, can be answered by a GIN index rather than by reading every session.
JPA cannot declare a GIN index, and CAS does not create it. The index is required and must be created manually: the
administrator runs the statement below once, after the ticket table has been created. Without it, those lookups still
work but read the whole table.

```sql
CREATE INDEX IF NOT EXISTS idx_ticket_attributes ON cas_tickets USING GIN (attributes jsonb_path_ops);
```

The index is part of the table, so it is needed again whenever the table is dropped and recreated, such as when starting
with a clean table as recommended above.

Tables created by earlier versions declare the column as `json`, which schema updates do not change, and session lookups
by attribute then fail. Start with a clean table, as recommended above, or convert the column first, which rewrites
the table and locks it while it runs:

```sql
ALTER TABLE cas_tickets ALTER COLUMN attributes TYPE jsonb USING attributes::jsonb;
```

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The tables that track locking operations come from [Spring Integration](https://spring.io/projects/spring-integration) JDBC support.
Spring Integration only creates them on its own for embedded databases; for any other database, either create them
up front from the schema scripts that ship with Spring Integration, or let CAS create them on startup with:

```properties
spring.integration.jdbc.initialize-schema=always
```
{: .cas-settings-linked}

Without these tables, every attempt to lock a ticket fails, and CAS cannot issue service tickets.

{% include_cached casproperties.html thirdPartyStartsWith="spring.integration.jdbc" %}
