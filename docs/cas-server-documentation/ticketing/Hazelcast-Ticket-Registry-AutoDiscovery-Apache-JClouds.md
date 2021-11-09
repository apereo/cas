---
layout: default
title: CAS - Hazelcast Ticket Registry - Apache JClouds Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Apache JClouds Auto Discovery

Hazelcast support in CAS may handle auto-discovery automatically
via [Apache jclouds®](https://jclouds.apache.org/). It is useful when
you do not want to provide or you cannot provide the list of possible
IP addresses for the members of the cluster. Apache jclouds® is an open
source multi-cloud toolkit for the Java platform that gives you the freedom
to create applications that are portable across clouds while giving you full
control to use cloud-specific features. To see the full list of supported
cloud environments, [please see this link](https://jclouds.apache.org/reference/providers/#compute).

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-jclouds" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.jclouds" %}
