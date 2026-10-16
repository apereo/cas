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
