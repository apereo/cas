---
layout: default
title: CAS - Web Flow Customization
category: Webflow Management
---

{% include variables.html %}

# Server-side Sessions

In the event that you wish to use server-side session storage for managing the
webflow session, you will need to enable this behavior
via CAS properties.

{% include_cached casproperties.html properties="cas.webflow.session"
thirdPartyStartsWith="spring.session"
excludes=".jdbc,.hazelcast,.mongodb,.redis" %}

Doing so will likely require you to also enable sticky sessions and/or session replication in a clustered deployment of
CAS.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>
Generally speaking, you do not need to enable server-side sessions unless you have a 
rather specialized deployment or are in need of features that store bits and pieces 
of data into a sever-backed session object. It is recommended that you stick with 
the default client-side session storage and only switch if and when mandated by a specific CAS behavior.</p></div>

## Storage Options

If you do wish to use server-side session storage, CAS supports the following options for storing webflow sessions:

| Topic           | Description                                                                      |
|-----------------|----------------------------------------------------------------------------------|
| Hazelcast       | [See this guide](Webflow-Customization-Sessions-ServerSide-Hazelcast.html).      |
| JDBC            | [See this guide](Webflow-Customization-Sessions-ServerSide-JDBC.html).           |
| MongoDb         | [See this guide](Webflow-Customization-Sessions-ServerSide-MongoDb.html).        |
| Redis           | [See this guide](Webflow-Customization-Sessions-ServerSide-Redis.html).          | 
| Ticket Registry | [See this guide](Webflow-Customization-Sessions-ServerSide-TicketRegistry.html). |
