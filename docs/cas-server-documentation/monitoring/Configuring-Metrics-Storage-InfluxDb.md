---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# InfluxDb Storage - CAS Metrics

By default, metrics are exported to Influx running on your local
machine. The location of the Influx server to use can be provided using:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.influx" %}
