---
layout: default
title: CAS - CosmosDb Ticket Registry
category: Ticketing
---

{% include variables.html %}

# CosmosDb Ticket Registry

CosmosDb support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-cosmosdb-ticket-registry" %}

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

{% include_cached casproperties.html properties="cas.ticket.registry.cosmos-db" %}
