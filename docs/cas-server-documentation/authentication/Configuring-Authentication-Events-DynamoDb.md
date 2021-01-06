---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# DynamoDb Authentication Events

Stores authentication events into a DynamoDb database.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-events-dynamodb" %}

{% include {{ version }}/dynamodb-configuration.md configKey="cas.events" %}

{% include {{ version }}/dynamodb-events-configuration.md %}
