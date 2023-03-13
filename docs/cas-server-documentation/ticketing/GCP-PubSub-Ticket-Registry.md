---
layout: default
title: CAS - GCP PubSub Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Google Cloud Pub/Sub Ticket Registry

This registry is very much an extension of the [default ticket registry](Default-Ticket-Registry.html). 
The difference is that ticket operations applied to the registry are broadcasted using [Google Cloud's PubSub](https://cloud.google.com/pubsub).

Each node keeps copies of ticket state on its own and only instructs others to keep their copy accurate by broadcasting messages and data associated with each. 
Each message and ticket registry instance running inside a CAS node in the cluster is tagged with a unique 
identifier in order to avoid endless looping behavior and recursive needless inbound operations.

<div class="alert alert-info">:information_source: <strong>Message Ordering</strong>
<p>This registry implementation requires you to enable message ordering via CAS settings.</p>
</div>

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gcp-ticket-registry" %}

## CAS Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.in-memory" thirdPartyStartsWith="spring.cloud.gcp.pubsub" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="health" healthIndicators="pubsub,pubsub-subscriber" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.google.cloud" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
