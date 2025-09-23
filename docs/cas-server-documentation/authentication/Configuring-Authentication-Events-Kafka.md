---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# Apache Kafka Authentication Events

Publishes authentication events into a Apache Kafka topic. This module only operates in a
write-only mode and does not consume or fetch any events from Apache Kafka topics.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-events-kafka" %}

{% include_cached casproperties.html properties="cas.events.kafka" %}

