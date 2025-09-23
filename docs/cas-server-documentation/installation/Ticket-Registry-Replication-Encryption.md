---
layout: default
title: CAS - Configuring Ticketing Components
category: Ticketing
---
{% include variables.html %}


# Ticket Registry Replication Encryption

The following ticket registries are able to support secure ticket replication
by encrypting and signing tickets:

* [Hazelcast](../ticketing/Hazelcast-Ticket-Registry.html)
* [Apache Ignite](../ticketing/Ignite-Ticket-Registry.html)
* [Apache Geode](../ticketing/Geode-Ticket-Registry.html)
* [Redis](../ticketing/Redis-Ticket-Registry.html)
* [MongoDb](../ticketing/MongoDb-Ticket-Registry.html)

<div class="alert alert-info">:information_source: <strong>Default Behavior</strong><p>Encryption by default is turned off
when you use the above ticket registries. It requires explicit configuration before it can be used.</p></div>

## Configuration

Each ticket registry configuration supports a cipher component that needs to be configured by the deployer.
The settings, algorithms and secret keys used for the cipher may be controlled via CAS settings.
Refer to the settings allotted for each registry to learn more about ticket encryption.
