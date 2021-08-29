---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# InfluxDb Authentication Events

Stores authentication events inside an InfluxDb instance.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-events-influxdb" %}

{% include_cached casproperties.html properties="cas.events.influx-db" %}

