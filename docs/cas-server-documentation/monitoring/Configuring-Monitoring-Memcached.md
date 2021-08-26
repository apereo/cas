---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# Memcached Monitoring

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-memcached-monitor" %}

{% include casproperties.html properties="cas.monitor.memcached" %}

The actual memcached implementation may be supported via one of the following options, expected to be defined in the overlay.

## Spymemcached

Enable support via the [spymemcached library](https://code.google.com/p/spymemcached/). 

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-memcached-spy" %}

## AWS ElastiCache

For clusters running the Memcached engine, ElastiCache supports Auto Discovery—the ability 
for client programs to automatically identify all of the nodes in a cache cluster, 
and to initiate and maintain connections to all of these nodes. 

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-memcached-aws-elasticache" %}

