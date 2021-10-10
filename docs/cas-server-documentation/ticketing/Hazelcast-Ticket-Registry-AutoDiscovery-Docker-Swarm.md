---
layout: default
title: CAS - Hazelcast Ticket Registry - Docker Swarm Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Docker Swarm Auto Discovery

This hazelcast discovery plugin provides a Docker Swarm mode based discovery strategy.

See [this link](https://github.com/bitsofinfo/hazelcast-docker-swarm-discovery-spi/) for more info.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-swarm" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.docker-swarm" %}
