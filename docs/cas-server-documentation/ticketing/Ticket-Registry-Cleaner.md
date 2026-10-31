---
layout: default
title: CAS - Ticket Registry Cleaner
description: "The background ticket registry cleaner that removes expired tickets, and when it should be turned off."
category: Ticketing
---

{% include variables.html %}

# Ticket Registry Cleaner

A background *cleaner* process is automatically scheduled to scan the chosen
registry implementation periodically and remove expired records based on configured threshold parameters.

{% include_cached casproperties.html properties="cas.ticket.registry.cleaner" %}
   
The ticket registry cleaner is generally useful in scenarios where the registry implementation is 
unable to auto-evict expired tokens and entries on its own via a background task. It may also be useful in scenarios
where the configured ticket expiration policy in CAS cannot be a direct one-to-one match for the ticket registry
if the policy is too parameterized or has many other dynamic conditions that would not be directly translatable for the ticket registry API.

Each run reads expired tickets in batches. Registries that store when each ticket expires, such as the
[JPA ticket registry](JPA-Ticket-Registry.html), are asked only for tickets whose expiration time has passed. The first
run after startup and every sixtieth run after it (hourly with the default one-minute schedule) read every ticket instead,
to catch tickets that expire for reasons other than time. Other registries return every ticket on each run, as before;
every ticket read is still checked against its expiration policy before it is removed.

Note that CAS itself will remove expired tickets on-demand when a ticket object is fetched and being processed.
The ticket registry cleaner use case primarily addresses stale tickets that would otherwise never be requested and processed
to go through the on-demand cleaning process as necessary.

<div class="alert alert-warning">:warning: <strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is 
best to keep the cleaner running on one designated CAS node only and turn it off on all others 
via CAS settings. Keeping the cleaner running on all nodes may likely lead to severe performance and locking issues.</p></div>
