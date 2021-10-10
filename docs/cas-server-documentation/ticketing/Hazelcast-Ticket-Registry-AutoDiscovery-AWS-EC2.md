---
layout: default
title: CAS - Hazelcast Ticket Registry - AWS EC2 Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - AWS EC2 Auto Discovery

Hazelcast support in CAS may handle EC2 auto-discovery automatically. It is useful when 
you do not want to provide or you cannot provide the list of possible IP addresses for 
the members of the cluster. You optionally also have the ability to specify partitioning 
group that would be zone aware. When using the zone-aware configuration, backups are 
created in the other AZs. Each zone will be accepted as one partition group. Using the 
AWS Discovery capability requires that you turn off and disable multicast and TCP/IP 
config in the CAS settings, which should be done automatically by CAS at runtime.

Support is enabled by the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-aws" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.aws" %}
