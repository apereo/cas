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

Each ticket is stored with its identifier as the document `_id`, so tickets are found through the index MongoDb
maintains for every collection. Earlier versions stored the identifier in a separate `ticketId` field with its own
`IDX_ID` index. At startup, CAS converts such documents in place and, when index updates are enabled, removes the
`IDX_ID` index. The conversion only touches documents in the earlier shape, can safely run again if interrupted, and
needs no downtime beyond the upgrade itself; all CAS nodes sharing the registry should be upgraded together, since a node
running an earlier version cannot read converted tickets. If an index CAS replaces cannot be rebuilt, CAS logs a warning
and puts the previous index back. Principal attributes are stored and indexed for ticket-granting tickets only, and
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
