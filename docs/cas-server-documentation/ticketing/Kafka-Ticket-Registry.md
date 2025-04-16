---
layout: default
title: CAS - Kafka Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Apache Kafka Ticket Registry

The Apache Kafka ticket registry is very much an extension of the [default ticket registry](Default-Ticket-Registry.html). 
The difference is that ticket operations applied to the registry are broadcasted using Kafka topics
to other listening CAS nodes. Each node keeps copies of ticket state on its own and only 
instructs others to keep their copy accurate by broadcasting messages and data associated with each. 
Each message and ticket registry instance running inside a CAS node in the cluster is tagged with a unique 
identifier in order to avoid endless looping behavior and recursive needless inbound operations.
    
The broadcast and pub/sub mechanism is backed by [Apache Kafka](https://kafka.apache.org/). Apache Kafka is a distributed 
event streaming platform designed for handling real-time data feeds. Kafka is highly scalable and fault-tolerant, 
making it ideal for building real-time data pipelines and streaming applications. 

Key features of Apache Kafka include:

- **Message Broker**: Kafka acts as a distributed messaging system that enables the publishing, storing, and processing of streams of data in real-time.
- **Producers and Consumers**: Data is produced by producers and consumed by consumers in Kafka. Producers send messages to Kafka topics, and consumers read messages from these topics.
- **Topics**: A topic is a category or stream name to which records are sent. Data is partitioned across multiple Kafka brokers based on topics.

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-kafka-ticket-registry" %}

## CAS Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.in-memory,cas.ticket.registry.kafka" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.apache.kafka" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
