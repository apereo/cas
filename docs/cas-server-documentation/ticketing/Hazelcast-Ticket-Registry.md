---
layout: default
title: CAS - Hazelcast Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry

Hazelcast Ticket Registry is a distributed ticket registry implementation
based on [Hazelcast distributed grid library](https://hazelcast.org/). The registry implementation is
cluster-aware and is able to auto-join a cluster of all the CAS nodes that expose this registry.
Hazelcast will use port auto-increment feature to assign a TCP port to each member of a cluster starting
from initially provided arbitrary port, which is typically `5701` by default.

Hazelcast will evenly distribute the ticket data among all the members of a cluster in a very
efficient manner. Also, by default, the data collection on each node is configured with 1 backup copy,
so that Hazelcast will use it to make strong data consistency guarantees i.e. the loss of data on
live nodes will not occur should any other *primary data owner* members die. The data will be
re-partitioned among the remaining live cluster members.

Support is enabled by the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-ticket-registry" %}

## Configuration

This module has a configuration strategy which by default auto-configures a hazelcast 
instance used by the ticket registry implementation to build and retrieve Hazelcast 
maps for its distributed tickets storage. Some aspects of hazelcast configuration in 
this auto-configuration mode are controlled by CAS properties.

{% include_cached {{ version }}/hazelcast-configuration.md configKey="cas.ticket.registry.hazelcast" %}

<div class="alert alert-warning"><strong>Session Monitoring</strong><p>Be aware that under 
very heavy load and given a very large collection of tickets 
over time, <a href="../monitoring/Configuring-Monitoring.html">session monitoring capabilities</a> of 
CAS that report back ticket statistics based on the underlying Hazelcast ticket 
registry may end up timing out. This is due to the concern that Hazelcast attempts 
to run distributed queries across the entire network to collect, analyze and 
aggregate tickets which may be still active or in flux. If you do experience 
this behavior, it likely is preferable to turn off the session monitor.
</p></div>

For more information on the Hazelcast configuration options available,
refer to [the Hazelcast documentation](https://docs.hazelcast.com/imdg/latest/)

### Security

Tokens and tickets that are managed by the Hazelcast ticket registry can be signed and encrypted.                        

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.crypto" %}

## Logging

To enable additional logging for the registry, configure the log4j 
configuration file to add the following levels:

```xml
...
<Logger name="com.hazelcast" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
