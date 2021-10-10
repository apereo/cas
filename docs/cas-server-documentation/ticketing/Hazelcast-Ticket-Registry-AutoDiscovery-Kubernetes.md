---
layout: default
title: CAS - Hazelcast Ticket Registry - Kubernetes Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Kubernetes Auto Discovery

This hazelcast discovery plugin provides the possibility to lookup IP addresses of other members by resolving
those requests against a [Kubernetes](http://kubernetes.io/) Service Discovery system.

This module supports two different options of resolving against the discovery registry:

- A request to the REST API
- DNS Lookup against a given headless DNS service name

See [this link](https://github.com/hazelcast/hazelcast-kubernetes) for more info.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-kubernetes" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.kubernetes" %}
