---
layout: default
title: CAS - Configuring Ticketing Components
category: Ticketing
---

{% include variables.html %}

# Ticketing

There are two core configurable ticketing components:

* `TicketRegistry` - Provides for durable ticket storage.
* `ExpirationPolicy` - Provides a policy framework for ticket expiration semantics.

## Ticket Registry

The deployment environment and technology expertise generally determine the 
particular `TicketRegistry` component. A cache-backed implementation is 
recommended for HA deployments, while the default in-memory registry may be suitable for small deployments.

### Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="ticketRegistry" casModule="cas-server-support-reports" %}

### How Do I Choose?

There are a wide range of ticket registries on the menu. The selection criteria are outlined below:

- Choose a technology that you are most familiar with and have the skills and patience to troubleshoot, tune and scale for the win. 
- Choose a technology that does not force your CAS configuration to be tied to any individual servers/nodes in the cluster, as this will present auto-scaling issues and manual effort.
- Choose a technology that works well with your network and firewall configuration and is performant and reliable enough based on your network topology.
- Choose a technology that shows promising results under *your expected load*, having run [performance and stress tests](../high_availability/High-Availability-Performance-Testing.html).
- Choose a technology that does not depend on outside processes and systems as much as possible, is self-reliant and self contained.

The above outlines suggestions and guidelines you may wish to consider. Each option presents various pros and cons and 
in the end, you must decide which drawbacks or advantages provide you with the best experience.

### Cache-Based Ticket Registries

Cached-based ticket registries provide a high-performance solution for ticket storage in high availability
deployments. Components for the following caching technologies are provided:

* [Default](Default-Ticket-Registry.html)
* [Hazelcast](Hazelcast-Ticket-Registry.html)
* [Apache Ignite](Ignite-Ticket-Registry.html)
* [Apache Geode](Geode-Ticket-Registry.html)

### Stateless Ticket Registries

Stateless ticket registries require no backend storage with a few caveats and limitations. 
Components for the following caching technologies are provided:

* [Stateless](Stateless-Ticket-Registry.html)

### Message-based Ticket Registries

* [AMQP](Messaging-AMQP-Ticket-Registry.html)
* [Google Cloud PubSub](GCP-PubSub-Ticket-Registry.html)
* [Apache Kafka](Kafka-Ticket-Registry.html)

### RDBMS Ticket Registries

RDBMS-based ticket registries provide a distributed ticket store across multiple CAS nodes.
Components for the following caching technologies are provided:

* [JPA](JPA-Ticket-Registry.html)

### NoSQL Ticket Registries

CAS also provides support for a variety of other databases, including Redis, MongoDb and Apache
Cassandra, for ticket storage and persistence:

* [Redis](Redis-Ticket-Registry.html)
* [MongoDb](MongoDb-Ticket-Registry.html)
* [DynamoDb](DynamoDb-Ticket-Registry.html)
* [Google Cloud Firestore](GCP-Firestore-Ticket-Registry.html)

### Secure Cache Replication

A number of cache-based ticket registries support secure replication of ticket data across the wire,
so that tickets are encrypted and signed on replication attempts to prevent sniffing and eavesdrops.
[See this guide](../installation/Ticket-Registry-Replication-Encryption.html) for more info.

### Ticket Registry Locking

A number of ticket registries support advanced distributed locking operations for highly concurrent requests,
to assist with synchronization of data and atomicity of operations. [See this guide](Ticket-Registry-Locking.html) 
for more info.

## Ticket Expiration Policies

CAS supports a pluggable and extensible policy framework to control the expiration policy of
ticket-granting tickets (TGT) and service tickets (ST).
[See this guide](Configuring-Ticket-Expiration-Policy.html) for details on how to configure the expiration policies.
