---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# StatsD Storage - CAS Metrics

The StatsD registry pushes metrics over UDP to a StatsD agent eagerly. By default,
metrics are exported to a StatsD agent running on your local machine.
The StatsD agent host and port to use can be provided using:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.statsd" %}
