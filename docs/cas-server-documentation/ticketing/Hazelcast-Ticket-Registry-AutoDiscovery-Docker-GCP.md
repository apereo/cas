---
layout: default
title: CAS - Hazelcast Ticket Registry - Google Cloud Platform (GCP) Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Google Cloud Platform (GCP) Auto Discovery

This hazelcast discovery plugin provides a Google Cloud Platform (GCP) discovery strategy.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-gcp" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.gcp" %}

Note that:

- Your GCP Service Account must have permissions to query for all the projects/zones specified in the configuration.
- If you don’t specify any of the properties, then CAS forms a cluster from all Hazelcast members running in the current project, in the current region.
