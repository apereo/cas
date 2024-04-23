---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# DynamoDb Monitoring

Monitor the status and availability of DynamoDb databases. This monitor is made available
with the usage of a DynamoDb-based module that would for instance manage [tickets](../ticketing/DynamoDb-Ticket-Registry.html)
or [application definitions](../services/DynamoDb-Service-Management.html), etc and is able to provide health data 
and statistics for each feature and/or active connection to the DynamoDb database.

{% include_cached actuators.html endpoints="health" healthIndicators="dynamoDbHealthIndicator" %}
