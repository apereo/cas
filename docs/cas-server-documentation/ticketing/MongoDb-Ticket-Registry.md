---
layout: default
title: CAS - MongoDb Ticket Registry
category: Ticketing
---

{% include variables.html %}

# MongoDb Ticket Registry

MongoDb ticket registry integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mongo-ticket-registry" %}

This registry stores tickets in one or more [MongoDb](https://www.mongodb.com/) instances.
Tickets are auto-converted and wrapped into document objects as JSON. Special indices are
created to let MongoDb handle the expiration of each document and cleanup tasks. Note that CAS generally tries to create the relevant collections automatically to manage different ticket types. 

When index updates are enabled, the ticket identifier index (`IDX_ID`) is unique. A deployment upgrading from an
earlier version rebuilds that index once at startup; should the rebuild fail, CAS logs a warning and puts the previous
index back so lookups keep using it. Principal attributes are stored and indexed for ticket-granting tickets only, and
the service index exists only on collections of tickets linked to a service; CAS removes those two indexes from other
collections when it updates indexes.

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.mongo" %}


## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following
levels:

```xml
...
<Logger name="com.mongo" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
