---
layout: default
title: CAS - Hazelcast Ticket Registry - Apache ZooKeeper Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Apache ZooKeeper Auto Discovery

This plugin provides a service-based discovery by using Apache Curator to
communicate with your Zookeeper server. Support is enabled by the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-zookeeper" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.zookeeper" %}

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](../ticketing/Ticket-Registry-Locking.html).
The database schemas and structures that track locking operations should be automatically created by CAS using
[Spring Integration](https://spring.io/projects/spring-integration) ZooKeeper support.
