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

## MySQL

MySQL limits an index key to 3072 bytes, which with the `utf8mb4` character set is 768 characters. On MySQL the
ticket table therefore declares `type` as `VARCHAR(255)`, `principal_id` as `VARCHAR(512)` and `parent_id` as
`VARCHAR(768)`, so that its indexes on the principal, the parent ticket and the ticket type with the principal can be
created; the `service` column keeps its length and is not indexed on MySQL. Tables created by earlier versions
declared longer columns, and MySQL refused every one of those indexes, so lookups by principal or parent ticket scanned
the whole table. Schema updates do not shorten existing columns; on an existing table, apply the change by hand:

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
