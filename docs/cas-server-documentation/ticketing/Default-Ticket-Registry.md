---
layout: default
title: CAS - Default Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Default Ticket Registry

The default registry uses a memory-backed internal concurrent map for ticket 
storage and retrieval, though there is also the option to use an implementation 
that is backed by a caching engine to gain slightly better performance when it comes to evicting expired tickets.

{% include_cached casproperties.html properties="cas.ticket.registry.in-memory" %}

## Eviction Policy

This ticket registry relies on a background job that is automatically scheduled to clean 
up after the registry and remove expired tickets. The cleaner will periodically examine 
the state of the registry to identify expired tickets, remove them from 
the registry and then execute relevant logout operations.
 
## Clustering

This registry does not by default preserve ticket state across restarts and is not a suitable solution
for clustered CAS environments that are deployed in active/active mode. Tickets are managed and stored
in the runtime memory that is bound to the CAS server node, which means a ticket object created and managed
by CAS server `A` cannot be found and accepted when a request for the same ticket is received by CAS server `B`.

The registry does however provide extension points for broadcasting the results of ticket operations.
A *Pub/Sub* type of setup can tap into such extension points to allow the registry to operate in clustered
environments, and to share ticket state across all CAS server nodes keeping them all in sync. 
Some ticket registries such as the [AMQP Ticket Registry](Messaging-AMQP-Ticket-Registry.html) are able to do so.
