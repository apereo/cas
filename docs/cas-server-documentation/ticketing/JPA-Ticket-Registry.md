---
layout: default
title: CAS - JPA Ticket Registry
category: Ticketing
---

{% include variables.html %}

# JPA Ticket Registry

The JPA Ticket Registry allows CAS to store client authenticated state
data (tickets) in a database back-end such as MySQL.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Using a relational database as
the back-end persistence choice for ticket registry state management is a fairly unnecessary and complicated
process. Unless you are already outfitted with clustered database technology and the resources to manage it,
the complexity is likely not worth the trouble.</p></div>

Support is enabled by adding the following module into the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-ticket-registry" %}

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.jpa" %}

## JPA Ticket Cleaner

A background *cleaner* process is also automatically scheduled to scan the chosen 
database periodically and remove expired records based on configured threshold parameters.

{% include_cached casproperties.html properties="cas.ticket.registry.cleaner" %}

<div class="alert alert-warning"><strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is 
best to keep the cleaner running on one designated CAS node only and turn it off on all others 
via CAS settings. Keeping the cleaner running on all nodes may likely lead to 
severe performance and locking issues.</p></div>

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The database schemas and tables that track locking operations should be automatically created by CAS using
[Spring Integration](https://spring.io/projects/spring-integration) JDBC support.

{% include_cached casproperties.html thirdParty="spring.integration.jdbc" %}
