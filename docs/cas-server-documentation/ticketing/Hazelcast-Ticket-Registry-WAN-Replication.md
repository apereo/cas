---
layout: default
title: CAS - Hazelcast Ticket Registry - WAN Replication
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - WAN Replication

Hazelcast WAN Replication allows you to keep multiple Hazelcast clusters 
in sync by replicating their state over WAN environments such as the Internet.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Using Hazelcast 
WAN Replication requires a Hazelcast Enterprise subscription. Make sure you 
have acquired the proper license, SDK and tooling from Hazelcast before 
activating this feature. Please contact Hazelcast for more information.</p></div>

Hazelcast supports two different operation modes of WAN Replication:

- Active-Passive: This mode is mostly used for failover scenarios where you want to replicate an active cluster to one or more passive clusters, for the purpose of maintaining a backup.
- Active-Active: Every cluster is equal, each cluster replicates to all other clusters. This is normally used to connect different clients to different clusters for the sake of the shortest path between client and server.

See [this page](https://hazelcast.com/products/wan-replication/) for more information.

Defining WAN replication endpoints in CAS is done using static endpoints and discovery.

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.wan-replication" %}
