---
layout: default
title: CAS - LDAP Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# LDAP Monitoring

Monitor the status and availability of an LDAP server.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap-monitor" %}

{% include_cached casproperties.html properties="cas.monitor.ldap" %}

{% include_cached actuators.html endpoints="health" healthIndicators="pooledLdapConnectionFactoryHealthIndicator" %}

## Cluster Topology

The [cluster topology](../high_availability/Cluster-Topology.html) lists the LDAP and Active Directory servers that
CAS connects to for any LDAP feature, each server once, with the features that use it and their pooled connections on
it, whether it answers, and what it reports about itself, such as its vendor, version and replication position.
For Active Directory, the other domain controllers of the domain are listed as well.

