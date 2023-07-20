---
layout: default
title: CAS - JPA Ticket Registry
category: Ticketing
---

{% include variables.html %}

# JPA Ticket Registry

The JPA Ticket Registry allows CAS to store tickets in a relational database back-end such as MySQL.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>Using a relational database as
the back-end persistence choice for ticket registry state management is a fairly unnecessary and complicated
process. Unless you are already outfitted with clustered database technology and the resources to manage it,
the complexity is likely not worth the trouble.</p></div>

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-ticket-registry" %}

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.jpa" %}

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The database schemas and tables that track locking operations should be automatically created by CAS using
[Spring Integration](https://spring.io/projects/spring-integration) JDBC support.

{% include_cached casproperties.html thirdPartyStartsWith="spring.integration.jdbc" %}
