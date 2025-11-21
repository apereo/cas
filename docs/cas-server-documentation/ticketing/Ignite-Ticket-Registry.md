---
layout: default
title: CAS - Apache Ignite Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Apache Ignite Ticket Registry

Apache Ignite integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ignite-ticket-registry" %}

This registry stores tickets in an [Apache Ignite](http://ignite.apache.org/) instance.

## Distributed Cache

Distributed caches are recommended for HA architectures since they offer fault tolerance in the ticket storage subsystem.

## TLS Replication

Apache Ignite supports replication over TLS for distributed caches 
composed of two or more nodes. To learn more about TLS replication with Ignite,
[see this resource](https://apacheignite.readme.io/docs/ssltls).

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.ignite" %}

## Troubleshooting

* You will need to ensure that network communication across CAS nodes is allowed and no firewall or other component is blocking traffic.
* If nodes external to CAS instances are utilized, ensure that each cache manager specifies a name that matches the Apache Ignite configuration itself.
* You may also need to adjust your expiration policy to allow for a larger time span, especially for service tickets depending on network
  traffic and communication delay across CAS nodes particularly in the event that a node is trying to join the cluster.
