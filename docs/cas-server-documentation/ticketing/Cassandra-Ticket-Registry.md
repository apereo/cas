---
layout: default
title: CAS - Cassandra Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Cassandra Ticket Registry

Cassandra integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-cassandra-ticket-registry" %}

This registry stores tickets in [Apache Cassandra](http://cassandra.apache.org/) instances. Tickets are expected to be found/stored in a `castickets` table with a default write consistency of `LOCAL_QUORUM` and read consistency of `ONE`.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="health" healthIndicators="cassandra" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.datastax.driver" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.cassandra" %}

