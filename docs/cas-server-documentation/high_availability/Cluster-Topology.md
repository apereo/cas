---
layout: default
title: CAS - Cluster Topology Management
description: "Discover and report the members of a clustered CAS deployment and its supporting components."
category: High Availability
---

{% include variables.html %}

# Cluster Topology Management

CAS can discover and report the topology of clustered deployment components used throughout the deployment, 
providing a unified view of participating members regardless of the underlying implementation. The discovery 
process identifies the members that currently make up the cluster, records their advertised network addresses and 
roles where applicable, and determines their operational status using the platform’s native health and topology 
mechanisms. This information is collected to present an accurate representation of the current cluster rather than 
relying solely on static configuration or connection endpoints.

The discovered topology is exposed through the CAS monitoring infrastructure, allowing administrators to inspect 
cluster membership, verify that individual members are reachable and healthy, and detect changes such as members 
joining, leaving, or becoming unavailable. This provides operational visibility into the distributed environment 
and helps diagnose connectivity, failover, replication, and high-availability issues by presenting the current 
cluster state from the perspective of the underlying distributed system.

Cluster topology support is available for the following features:

- [MongoDb Ticket Registry](../ticketing/MongoDb-Ticket-Registry.html)
- [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html)
- [Hazelcast Ticket Registry](../ticketing/Hazelcast-Ticket-Registry.html)
- [Apache Ignite Ticket Registry](../ticketing/Ignite-Ticket-Registry.html)
- [Apache Kafka Ticket Registry](../ticketing/Kafka-Ticket-Registry.html)
- [Apache Pulsar Ticket Registry](../ticketing/Pulsar-Ticket-Registry.html)
- [AMQP Ticket Registry](../ticketing/Messaging-AMQP-Ticket-Registry.html)
- [Apache Geode Ticket Registry](../ticketing/Geode-Ticket-Registry.html)
- [LDAP & Active Directory](../monitoring/Configuring-Monitoring-LDAP.html)

With [LDAP monitoring](../monitoring/Configuring-Monitoring-LDAP.html) included, the LDAP members are the directory
servers that CAS connects to for any LDAP feature, such as authentication,
attribute resolution or a service registry. Each server is listed once, with the features that use it and how many of
their pooled connections are on it, whether it answers and when it last failed to, and what the server reports about
itself: its vendor and version, its naming contexts and, where the server shares it, how far its replica has come
compared to the others. For Active Directory, the site of each domain controller is reported and the other domain
controllers of the domain are listed as members as well. Servers found through DNS SRV records are included.

## Custom Cluster Topology

If you wish to design your own cluster topology discovery mechanism, you
may plug in a custom implementation of the `ClusterTopologyManager`:

```java
@Bean
public ClusterTopologyManager myClusterTopologyManager() {
    return new MyClusterTopologyManager();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="clusterTopology" %}
