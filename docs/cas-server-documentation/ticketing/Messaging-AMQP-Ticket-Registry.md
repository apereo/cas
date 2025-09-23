---
layout: default
title: CAS - Messaging (AMQP) Ticket Registry
category: Ticketing
---

{% include variables.html %}

# AMQP Ticket Registry

CAS can be enabled with a variety of messaging systems in order to distribute and share ticket data: 
from simplified use of the AMQP API to a complete infrastructure to receive messages asynchronously.

This registry is very much an extension of the [default ticket registry](Default-Ticket-Registry.html). 
The difference is that ticket operations applied to the registry are broadcasted using a messaging queue 
to other listening CAS nodes on the queue. Each node keeps copies of ticket state on its own and only 
instructs others to keep their copy accurate by broadcasting messages and data associated with each. 
Each message and ticket registry instance running inside a CAS node in the cluster is tagged with a unique 
identifier in order to avoid endless looping behavior and recursive needless inbound operations.
    
The broadcast and the message queue is backed by the Advanced Message Queuing Protocol (AMQP) protocol. This is a platform-neutral, 
wire-level protocol for message-oriented middleware. The implementation of this protocol is backed by [RabbitMQ](https://www.rabbitmq.com/). 
This is a lightweight, reliable, scalable, and portable message broker based on the AMQP protocol. CAS uses RabbitMQ to communicate through the AMQP protocol.

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-amqp-ticket-registry" %}

## CAS Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.in-memory" thirdPartyStartsWith="spring.rabbitmq" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="health" healthIndicators="rabbitHealthIndicator" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.springframework.amqp" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>

<Logger name="com.rabbitmq" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
