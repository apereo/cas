---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# DynamoDb Authentication Events

Stores authentication events into a DynamoDb database.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-events-dynamodb" %}

{% include_cached casproperties.html properties="cas.events.dynamodb-db" %}

