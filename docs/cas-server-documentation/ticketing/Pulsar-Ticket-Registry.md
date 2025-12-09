---
layout: default
title: CAS - Apache Pulsar Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Apache Pulsar Ticket Registry

The Apache Pulsar ticket registry is very much an extension of the [default ticket registry](Default-Ticket-Registry.html).
The difference is that ticket operations applied to the registry are broadcasted using Pulsar topics
to other listening CAS nodes. Each node keeps copies of ticket state on its own and only
instructs others to keep their copy accurate by broadcasting messages and data associated with each.
Each message and ticket registry instance running inside a CAS node in the cluster is tagged with a unique
identifier in order to avoid endless looping behavior and recursive needless inbound operations.

The broadcast and pub/sub mechanism is backed by [Apache Pulsar](https://pulsar.apache.org/). Apache Pulsar is 
an open-source, distributed messaging and streaming platform built for the cloud.

In Apache Pulsar, topics do not need to be created beforehand. They are created automatically on first use, as long as:

- The namespace exists, and
- Auto-creation is enabled in the broker configuration (it is enabled by default in most local/docker setups).

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pulsar-ticket-registry" %}

## CAS Configuration

{% include_cached casproperties.html
    thirdPartyStartsWith="spring.pulsar"
    properties="cas.ticket.registry.in-memory,cas.ticket.registry.pulsar" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.springframework.pulsar" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
<Logger name="org.apache.pulsar" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
